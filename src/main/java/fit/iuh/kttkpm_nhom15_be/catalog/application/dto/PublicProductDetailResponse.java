package fit.iuh.kttkpm_nhom15_be.catalog.application.dto;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductDetailResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.SemanticReferenceResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Media;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Product;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Variant;

import java.util.List;

public record PublicProductDetailResponse(
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
    List<Variant> variants,
    List<Media> media,
    List<SemanticReferenceResponse> ingredients,
    List<SemanticReferenceResponse> skinTypes,
    List<SemanticReferenceResponse> concerns,
    List<SemanticReferenceResponse> tags
) {
    public static PublicProductDetailResponse from(Product product, ProductDetailResponse detail) {
        return new PublicProductDetailResponse(
            product.getId(),
            product.getTypeId(),
            product.getName(),
            product.getSlug(),
            product.getDescriptionMd(),
            detail.shortDescription(),
            detail.brandId(),
            detail.brandName(),
            product.isCustomizable(),
            product.isActive(),
            product.getVariants(),
            product.getMedia(),
            detail.ingredients(),
            detail.skinTypes(),
            detail.concerns(),
            detail.tags()
        );
    }
}
