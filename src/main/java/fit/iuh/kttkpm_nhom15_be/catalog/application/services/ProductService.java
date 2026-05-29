package fit.iuh.kttkpm_nhom15_be.catalog.application.services;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.ProductResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.ProductResponse.ProductVariantResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductDetailResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.SemanticReferenceResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchProductItemDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchProductsRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.usecases.query.SearchProductsUseCase;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiNotFoundException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

  private static final int AGENT_SEARCH_SIZE = 8;

  private final SearchProductsUseCase searchProductsUseCase;
  private final CatalogAdminService catalogAdminService;

  public List<ProductResponse> searchProducts(
    String keyword,
    String category,
    String skinType,
    String brand,
    BigDecimal maxPrice
  ) {
    String query = buildSearchQuery(keyword, category, skinType, brand);
    var result = searchProductsUseCase.execute(new SearchProductsRequest(
      query,
      null,
      null,
      maxPrice,
      true,
      "relevance",
      0,
      AGENT_SEARCH_SIZE
    ));

    return result.items().stream()
      .map(this::fromSearchItem)
      .toList();
  }

  public ProductResponse getProductDetail(String productId, String productName) {
    String resolvedProductId = resolveProductId(productId, productName, false);
    return fromDetail(catalogAdminService.getProductDetail(resolvedProductId));
  }

  public ProductResponse findProductForCart(String productId, String productName) {
    String resolvedProductId = resolveProductId(productId, productName, true);
    return fromDetail(catalogAdminService.getProductDetail(resolvedProductId));
  }

  private String resolveProductId(String productId, String productName, boolean requireInStock) {
    String normalizedProductId = trimToNull(productId);
    if (normalizedProductId != null) {
      return normalizedProductId;
    }

    String normalizedProductName = trimToNull(productName);
    if (normalizedProductName == null) {
      throw new ApiValidationException("Bạn vui lòng cho mình biết tên hoặc mã sản phẩm.");
    }

    var result = searchProductsUseCase.execute(new SearchProductsRequest(
      normalizedProductName,
      null,
      null,
      null,
      requireInStock ? true : null,
      "relevance",
      0,
      5
    ));

    List<SearchProductItemDTO> items = result.items();
    if (items == null || items.isEmpty()) {
      throw new ApiNotFoundException("Không tìm thấy sản phẩm phù hợp với tên: " + normalizedProductName);
    }

    String lowerProductName = normalizedProductName.toLowerCase(Locale.ROOT);
    return items.stream()
      .filter(item -> item.productName() != null && item.productName().trim().toLowerCase(Locale.ROOT).equals(lowerProductName))
      .findFirst()
      .or(() -> items.size() == 1 ? items.stream().findFirst() : java.util.Optional.empty())
      .map(SearchProductItemDTO::productId)
      .orElseThrow(() -> new ApiValidationException("Mình tìm thấy nhiều sản phẩm gần giống. Bạn vui lòng nhập tên sản phẩm cụ thể hơn."));
  }

  private ProductResponse fromSearchItem(SearchProductItemDTO item) {
    return new ProductResponse(
      item.productId(),
      item.typeId(),
      item.typeName(),
      item.productName(),
      item.slug(),
      null,
      null,
      null,
      item.minPrice(),
      item.maxPrice(),
      null,
      item.inStock(),
      item.averageRating(),
      item.reviewCount(),
      item.soldCount(),
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      null
    );
  }

  private ProductResponse fromDetail(ProductDetailResponse detail) {
    List<ProductVariantResponse> variants = detail.variants() == null
      ? List.of()
      : detail.variants().stream()
        .map(variant -> new ProductVariantResponse(
          variant.id(),
          variant.sku(),
          variant.price(),
          variant.stockQuantity(),
          variant.active()
        ))
        .toList();

    ProductVariantResponse defaultVariant = variants.stream()
      .filter(variant -> variant.active() && variant.stockQuantity() > 0)
      .min(Comparator.comparing(ProductVariantResponse::price, Comparator.nullsLast(Comparator.naturalOrder())))
      .orElse(null);

    return new ProductResponse(
      detail.id(),
      detail.typeId(),
      null,
      detail.name(),
      detail.slug(),
      detail.descriptionMd(),
      detail.shortDescription(),
      detail.brandName(),
      detail.minPrice(),
      detail.maxPrice(),
      detail.totalStock(),
      detail.totalStock() > 0,
      detail.averageRating(),
      detail.reviewCount(),
      detail.soldCount(),
      semanticNames(detail.ingredients()),
      semanticNames(detail.skinTypes()),
      semanticNames(detail.concerns()),
      semanticNames(detail.tags()),
      variants,
      defaultVariant
    );
  }

  private List<String> semanticNames(List<SemanticReferenceResponse> references) {
    if (references == null || references.isEmpty()) {
      return List.of();
    }
    return references.stream()
      .map(SemanticReferenceResponse::name)
      .map(this::trimToNull)
      .filter(Objects::nonNull)
      .toList();
  }

  private String buildSearchQuery(String keyword, String category, String skinType, String brand) {
    Set<String> terms = new LinkedHashSet<>();
    addTerm(terms, keyword);
    addTerm(terms, category);
    addTerm(terms, skinType);
    addTerm(terms, brand);
    return terms.isEmpty() ? null : String.join(" ", terms);
  }

  private void addTerm(Set<String> terms, String value) {
    String normalized = trimToNull(value);
    if (normalized != null) {
      terms.add(normalized);
    }
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
