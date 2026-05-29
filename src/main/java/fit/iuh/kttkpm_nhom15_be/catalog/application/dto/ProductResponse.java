package fit.iuh.kttkpm_nhom15_be.catalog.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
  String id,
  String typeId,
  String typeName,
  String name,
  String slug,
  String descriptionMd,
  String shortDescription,
  String brandName,
  BigDecimal minPrice,
  BigDecimal maxPrice,
  Long totalStock,
  boolean inStock,
  BigDecimal averageRating,
  long reviewCount,
  long soldCount,
  List<String> ingredients,
  List<String> skinTypes,
  List<String> concerns,
  List<String> tags,
  List<ProductVariantResponse> variants,
  ProductVariantResponse defaultVariant
) {
  public record ProductVariantResponse(
    String id,
    String sku,
    BigDecimal price,
    int stockQuantity,
    boolean active
  ) {}
}
