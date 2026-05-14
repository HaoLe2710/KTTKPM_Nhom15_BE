package fit.iuh.kttkpm_nhom15_be.promotions.infrastructure.facades;

import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.AppliedPromotionDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.interfaces.PromotionFacade;
import fit.iuh.kttkpm_nhom15_be.promotions.application.pricing.PromotionCalculator;
import fit.iuh.kttkpm_nhom15_be.promotions.application.pricing.PromotionCalculatorFactory;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.exceptions.PromotionNotApplicableException;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.repositories.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PromotionFacadeImpl implements PromotionFacade {
    private final PromotionRepository promotionRepository;
    private final PromotionCalculatorFactory promotionCalculatorFactory;

    @Override
    @Transactional(readOnly = true)
    public AppliedPromotionDTO validateAndCalculate(String code, OrderCartDTO cart) {
        String normalizedCode = normalizeCode(code);
        Promotion promotion = promotionRepository.findByCode(normalizedCode)
            .orElseThrow(() -> new PromotionNotApplicableException("Promotion code is invalid."));

        if (!isCommonlyApplicable(promotion)) {
            throw new PromotionNotApplicableException("Promotion code is not active or no longer available.");
        }

        PromotionCalculator calculator = promotionCalculatorFactory.getCalculator(promotion.getType());
        if (!calculator.isApplicable(promotion, cart)) {
            throw new PromotionNotApplicableException("Promotion code does not apply to this order.");
        }

        BigDecimal discountAmount = calculator.calculateDiscount(promotion, cart);
        if (discountAmount == null || discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PromotionNotApplicableException("Promotion code does not produce a valid discount.");
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
        LocalDateTime now = LocalDateTime.now();
        boolean inWindow = !now.isBefore(promotion.getStartDate()) && !now.isAfter(promotion.getEndDate());
        boolean underUsageLimit = promotion.getUsageLimit() == null || promotion.getUsedCount() < promotion.getUsageLimit();
        return promotion.isActive() && inWindow && underUsageLimit;
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new PromotionNotApplicableException("Promotion code is required.");
        }
        return code.trim().toUpperCase();
    }
}
