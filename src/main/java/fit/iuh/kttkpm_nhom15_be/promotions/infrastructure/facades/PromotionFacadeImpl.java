package fit.iuh.kttkpm_nhom15_be.promotions.infrastructure.facades;

import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.AppliedPromotionDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartItemDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.interfaces.PromotionFacade;
import fit.iuh.kttkpm_nhom15_be.promotions.application.pricing.PromotionCalculator;
import fit.iuh.kttkpm_nhom15_be.promotions.application.pricing.PromotionCalculatorFactory;
import fit.iuh.kttkpm_nhom15_be.promotions.application.support.PromotionCustomerEligibility;
import fit.iuh.kttkpm_nhom15_be.promotions.application.support.PromotionTime;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.exceptions.PromotionNotApplicableException;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.repositories.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PromotionFacadeImpl implements PromotionFacade {
    private final PromotionRepository promotionRepository;
    private final PromotionCalculatorFactory promotionCalculatorFactory;

    @Override
    @Transactional(readOnly = true)
    public AppliedPromotionDTO validateAndCalculate(String code, OrderCartDTO cart) {
        Promotion promotion = findCommonlyApplicablePromotion(code);

        AppliedPromotionDTO appliedPromotion = calculateIfApplicable(promotion, cart);
        if (appliedPromotion == null) {
            throw new PromotionNotApplicableException("Promotion code does not apply to this order.");
        }

        return appliedPromotion;
    }

    @Override
    @Transactional(readOnly = true)
    public AppliedPromotionDTO validateOrderDiscountAndCalculate(String code, OrderCartDTO cart) {
        return validateOrderDiscountAndCalculate(code, cart, null);
    }

    @Override
    @Transactional(readOnly = true)
    public AppliedPromotionDTO validateOrderDiscountAndCalculate(String code, OrderCartDTO cart, String userId) {
        Promotion promotion = findCommonlyApplicablePromotion(code);
        if (promotion.getType() != PromotionType.ORDER_DISCOUNT) {
            throw new PromotionNotApplicableException("Promotion code is not an order voucher.");
        }
        if (!PromotionCustomerEligibility.isEligibleForUser(promotion, userId)) {
            throw new PromotionNotApplicableException("Voucher chỉ áp dụng cho tài khoản được chỉ định.");
        }

        AppliedPromotionDTO appliedPromotion = calculateIfApplicable(promotion, cart);
        if (appliedPromotion == null) {
            throw new PromotionNotApplicableException("Promotion code does not apply to this order.");
        }

        return appliedPromotion;
    }

    @Override
    @Transactional(readOnly = true)
    public AppliedPromotionDTO findBestAutomaticProductDiscount(OrderCartDTO cart) {
        return findAutomaticProductDiscounts(cart).stream()
            .max(Comparator.comparing(AppliedPromotionDTO::discountAmount))
            .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppliedPromotionDTO> findAutomaticProductDiscounts(OrderCartDTO cart) {
        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            return List.of();
        }

        LocalDateTime now = PromotionTime.now();
        List<Promotion> activeProductPromotions = promotionRepository.findActiveByType(PromotionType.PRODUCT_DISCOUNT, now);
        Map<String, AppliedPromotionDTO> discountsByPromotionId = new LinkedHashMap<>();

        for (OrderCartItemDTO item : cart.items()) {
            OrderCartDTO itemCart = new OrderCartDTO(item.lineTotal(), List.of(item));
            AppliedPromotionDTO bestItemDiscount = activeProductPromotions.stream()
                .map(promotion -> calculateIfApplicable(promotion, itemCart))
                .filter(applied -> applied != null && applied.discountAmount().compareTo(BigDecimal.ZERO) > 0)
                .max(Comparator.comparing(AppliedPromotionDTO::discountAmount))
                .orElse(null);

            if (bestItemDiscount != null) {
                discountsByPromotionId.merge(
                    bestItemDiscount.promotionId(),
                    bestItemDiscount,
                    (current, next) -> new AppliedPromotionDTO(
                        current.promotionId(),
                        current.promotionCode(),
                        current.discountAmount().add(next.discountAmount())
                    )
                );
            }
        }

        return discountsByPromotionId.values().stream().toList();
    }

    private AppliedPromotionDTO calculateIfApplicable(Promotion promotion, OrderCartDTO cart) {
        PromotionCalculator calculator = promotionCalculatorFactory.getCalculator(promotion.getType());
        if (!calculator.isApplicable(promotion, cart)) {
            return null;
        }

        BigDecimal discountAmount = calculator.calculateDiscount(promotion, cart);
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return new AppliedPromotionDTO(
            promotion.getId(),
            promotion.getCode(),
            discountAmount.min(cart.subtotal())
        );
    }

    @Override
    @Transactional
    public void markPromotionUsed(String promotionId) {
        promotionRepository.incrementUsedCount(promotionId);
    }

    private boolean isCommonlyApplicable(Promotion promotion) {
        LocalDateTime now = PromotionTime.now();
        boolean inWindow = !now.isBefore(promotion.getStartDate()) && !now.isAfter(promotion.getEndDate());
        boolean underUsageLimit = promotion.getUsageLimit() == null || promotion.getUsedCount() < promotion.getUsageLimit();
        return promotion.isActive() && inWindow && underUsageLimit;
    }

    private Promotion findCommonlyApplicablePromotion(String code) {
        String normalizedCode = normalizeCode(code);
        Promotion promotion = promotionRepository.findByCode(normalizedCode)
            .orElseThrow(() -> new PromotionNotApplicableException("Promotion code is invalid."));

        if (!isCommonlyApplicable(promotion)) {
            throw new PromotionNotApplicableException("Promotion code is not active or no longer available.");
        }

        return promotion;
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new PromotionNotApplicableException("Promotion code is required.");
        }
        return code.trim().toUpperCase();
    }
}
