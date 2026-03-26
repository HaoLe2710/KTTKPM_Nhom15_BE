package fit.iuh.kttkpm_nhom15_be.search.application.dto;

import java.math.BigDecimal;

public record SearchProductItemDTO(
  String productId,
  String slug,
  String productName,
  String typeId,
  String typeName,
  BigDecimal minPrice,
  BigDecimal maxPrice,
  boolean inStock,
  int activeVariantCount,
  BigDecimal averageRating,
  int reviewCount,
  int soldCount,
  double totalScore
) {}
