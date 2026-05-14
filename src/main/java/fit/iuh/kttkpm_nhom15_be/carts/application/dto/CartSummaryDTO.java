package fit.iuh.kttkpm_nhom15_be.carts.application.dto;

import java.math.BigDecimal;

public record CartSummaryDTO(
  int totalItems,
  BigDecimal subtotal
) {}
