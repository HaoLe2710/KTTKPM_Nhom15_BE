package fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class CatalogAdminDtos {

  private CatalogAdminDtos() {
  }

  public record ToggleActiveRequest(
    @NotNull Boolean active
  ) {
  }

  public record SemanticReferenceResponse(
    String id,
    String code,
    String name
  ) {
  }

  public record SemanticMasterListItemResponse(
    String id,
    String code,
    String name,
    String slug,
    String description,
    String normalizedName,
    String inciName,
    String logoUrl,
    boolean active,
    long usageCount
  ) {
  }

  public record BrandWriteRequest(
    @NotBlank String code,
    @NotBlank String name,
    String slug,
    String description,
    String logoUrl,
    Boolean isActive
  ) {
  }

  public record IngredientWriteRequest(
    @NotBlank String code,
    @NotBlank String name,
    String normalizedName,
    String inciName,
    String description,
    Boolean isActive
  ) {
  }

  public record SemanticMasterWriteRequest(
    @NotBlank String code,
    @NotBlank String name,
    String description,
    Boolean isActive
  ) {
  }

  public record VariantOptionAssignmentRequest(
    @NotBlank String optionId,
    @NotBlank String valueId
  ) {
  }

  public record ProductVariantCreateRequest(
    @NotBlank String sku,
    @NotNull @Min(0) BigDecimal price,
    @NotNull @Min(0) Integer stockQuantity,
    @NotNull Boolean isActive,
    @Valid List<VariantOptionAssignmentRequest> options
  ) {
  }

  public record ProductCreateRequest(
    @NotBlank String typeId,
    @NotBlank String name,
    String slug,
    String descriptionMd,
    String shortDescription,
    String brandId,
    @NotNull Boolean isCustomizable,
    @NotNull Boolean isActive,
    List<@NotBlank String> ingredientIds,
    List<@NotBlank String> skinTypeIds,
    List<@NotBlank String> concernIds,
    List<@NotBlank String> tagIds,
    @Valid @NotEmpty List<ProductVariantCreateRequest> variants
  ) {
  }

  public record ProductUpdateRequest(
    @NotBlank String typeId,
    @NotBlank String name,
    String slug,
    String descriptionMd,
    String shortDescription,
    String brandId,
    @NotNull Boolean isCustomizable,
    @NotNull Boolean isActive,
    List<@NotBlank String> ingredientIds,
    List<@NotBlank String> skinTypeIds,
    List<@NotBlank String> concernIds,
    List<@NotBlank String> tagIds
  ) {
  }

  public record VariantCreateRequest(
    @NotBlank String productId,
    @NotBlank String sku,
    @NotNull @Min(0) BigDecimal price,
    @NotNull @Min(0) Integer stockQuantity,
    @NotNull Boolean isActive,
    @Valid List<VariantOptionAssignmentRequest> options
  ) {
  }

  public record VariantUpdateRequest(
    @NotBlank String sku,
    @NotNull @Min(0) BigDecimal price,
    @NotNull @Min(0) Integer stockQuantity,
    @NotNull Boolean isActive,
    @Valid List<VariantOptionAssignmentRequest> options
  ) {
  }

  public record VariantStockPatchRequest(
    @NotNull @Min(0) Integer stockQuantity
  ) {
  }

  public record ProductListItemResponse(
    String id,
    String typeId,
    String name,
    String slug,
    boolean active,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    long totalStock,
    long activeVariantCount,
    String projectionStaleness,
    LocalDateTime projectionUpdatedAt
  ) {
  }

  public record OptionAssignmentResponse(
    String optionId,
    String optionValueId,
    String optionValueLabel
  ) {
  }

  public record VariantSummaryResponse(
    String id,
    String sku,
    String normalizedSku,
    BigDecimal price,
    int stockQuantity,
    boolean active,
    List<OptionAssignmentResponse> options
  ) {
  }

  public record SearchStatusResponse(
    boolean projectionExists,
    LocalDateTime projectionUpdatedAt,
    LocalDateTime sourceUpdatedAt,
    long projectionVersion,
    String staleness
  ) {
  }

  public record ProductDetailResponse(
    String id,
    String typeId,
    String name,
    String slug,
    String descriptionMd,
    String shortDescription,
    String brandId,
    String brandName,
    boolean customizable,
    boolean active,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    long totalStock,
    long activeVariantCount,
    long reviewCount,
    BigDecimal averageRating,
    long soldCount,
    List<SemanticReferenceResponse> ingredients,
    List<SemanticReferenceResponse> skinTypes,
    List<SemanticReferenceResponse> concerns,
    List<SemanticReferenceResponse> tags,
    SearchStatusResponse searchStatus,
    List<VariantSummaryResponse> variants
  ) {
  }

  public record CreatedResourceResponse(
    String id
  ) {
  }

  public record OptionValueWriteRequest(
    @NotBlank String value,
    Integer sortOrder,
    Boolean isActive
  ) {
  }

  public record OptionWriteRequest(
    @NotBlank String code,
    @NotBlank String name,
    Boolean isActive,
    @Valid List<OptionValueWriteRequest> values
  ) {
  }

  public record OptionListItemResponse(
    String id,
    String code,
    String name,
    boolean active,
    long valueCount,
    long usageCount
  ) {
  }

  public record OptionValueListItemResponse(
    String id,
    String optionId,
    String value,
    int sortOrder,
    boolean active,
    long usageCount
  ) {
  }

  public record CatalogHealthIssueCountResponse(
    String issueCode,
    long count
  ) {
  }

  public record CatalogHealthResponse(
    List<CatalogHealthIssueCountResponse> issues
  ) {
  }
}
