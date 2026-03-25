package fit.iuh.kttkpm_nhom15_be.promotions.application.dto;

import java.math.BigDecimal;

public record OrderCartItemDTO(
    String variantId,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal
) {
}
