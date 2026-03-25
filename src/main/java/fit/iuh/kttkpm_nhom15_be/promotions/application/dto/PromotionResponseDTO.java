package fit.iuh.kttkpm_nhom15_be.promotions.application.dto;

import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;

import java.time.LocalDateTime;
import java.util.Map;

public record PromotionResponseDTO(
    String id,
    String code,
    String name,
    PromotionType type,
    Map<String, Object> config,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Integer usageLimit,
    int usedCount,
    boolean active
) {
    public static PromotionResponseDTO from(Promotion promotion) {
        return new PromotionResponseDTO(
            promotion.getId(),
            promotion.getCode(),
            promotion.getName(),
            promotion.getType(),
            promotion.getConfig(),
            promotion.getStartDate(),
            promotion.getEndDate(),
            promotion.getUsageLimit(),
            promotion.getUsedCount(),
            promotion.isActive()
        );
    }
}
