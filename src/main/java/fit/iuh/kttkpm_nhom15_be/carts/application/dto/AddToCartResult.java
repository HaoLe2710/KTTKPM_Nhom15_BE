package fit.iuh.kttkpm_nhom15_be.carts.application.dto;

import java.math.BigDecimal;

public record AddToCartResult(
  boolean success,
  String message,
  String userId,
  String productId,
  String productName,
  String variantId,
  String sku,
  int quantity,
  BigDecimal unitPrice,
  CartSummaryDTO cartSummary
) {}
