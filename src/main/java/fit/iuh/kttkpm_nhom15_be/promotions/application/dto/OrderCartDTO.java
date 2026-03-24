package fit.iuh.kttkpm_nhom15_be.promotions.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderCartDTO(
    BigDecimal subtotal,
    List<OrderCartItemDTO> items
) {
}
