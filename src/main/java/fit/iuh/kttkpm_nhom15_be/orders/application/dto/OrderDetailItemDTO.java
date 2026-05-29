package fit.iuh.kttkpm_nhom15_be.orders.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public record OrderDetailItemDTO(
  String itemId,
  String productId,
  String variantId,
  String sku,
  String name,
  String imageUrl,
  Map<String, Object> optionsSnapshot,
  int quantity,
  BigDecimal unitPrice,
  BigDecimal lineTotal,
  boolean canReview,
  boolean alreadyReviewed,
  Integer reviewRating,
  String reviewContent,
  LocalDateTime reviewCreatedAt,
  String reviewMessage
) {}
