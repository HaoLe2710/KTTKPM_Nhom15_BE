package fit.iuh.kttkpm_nhom15_be.promotions.application.dto;

import java.math.BigDecimal;

public record AppliedPromotionDTO(
    String promotionId,
    String promotionCode,
    BigDecimal discountAmount
) {
}
