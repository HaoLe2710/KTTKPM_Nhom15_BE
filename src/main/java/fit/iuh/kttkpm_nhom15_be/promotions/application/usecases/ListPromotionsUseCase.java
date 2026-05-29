package fit.iuh.kttkpm_nhom15_be.promotions.application.usecases;

import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.PromotionResponseDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.support.PromotionCustomerEligibility;
import fit.iuh.kttkpm_nhom15_be.promotions.application.support.PromotionTime;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.repositories.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ListPromotionsUseCase {
    private final PromotionRepository promotionRepository;

    @Transactional(readOnly = true)
    public List<PromotionResponseDTO> execute(PromotionType type, boolean activeOnly) {
        List<Promotion> promotions;
        if (activeOnly) {
            LocalDateTime now = PromotionTime.now();
            promotions = type == null
                ? promotionRepository.findActive(now)
                : promotionRepository.findActiveByType(type, now);
        } else {
            promotions = type == null
                ? promotionRepository.findAll()
                : promotionRepository.findByType(type);
        }

        return promotions.stream()
            .map(PromotionResponseDTO::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PromotionResponseDTO> executeAssignedOrderVouchers(String userId) {
        if (userId == null || userId.isBlank() || userId.startsWith("guest-")) {
            return List.of();
        }

        return promotionRepository.findActiveByType(PromotionType.ORDER_DISCOUNT, PromotionTime.now()).stream()
            .filter(promotion -> PromotionCustomerEligibility.isEligibleForUser(promotion, userId))
            .map(this::toCustomerVoucherResponse)
            .toList();
    }

    private PromotionResponseDTO toCustomerVoucherResponse(Promotion promotion) {
        Map<String, Object> publicConfig = new LinkedHashMap<>(
            promotion.getConfig() == null ? Map.of() : promotion.getConfig()
        );
        publicConfig.remove(PromotionCustomerEligibility.TARGET_CUSTOMER_IDS_KEY);
        publicConfig.remove("customerIds");
        publicConfig.remove("targetUserIds");

        return new PromotionResponseDTO(
            promotion.getId(),
            promotion.getCode(),
            promotion.getName(),
            promotion.getType(),
            publicConfig,
            promotion.getStartDate(),
            promotion.getEndDate(),
            promotion.getUsageLimit(),
            promotion.getUsedCount(),
            promotion.isActive()
        );
    }
}
