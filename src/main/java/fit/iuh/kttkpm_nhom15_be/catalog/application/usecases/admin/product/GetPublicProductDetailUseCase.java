package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.PublicProductDetailResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.services.CatalogAdminService;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Product;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheNames;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPublicProductDetailUseCase {

  private final ProductRepository productRepository;
  private final CatalogAdminService catalogAdminService;

  @Cacheable(
    cacheNames = CacheNames.PRODUCT_DETAIL,
    key = "T(fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheKeys).productDetail(#productId)"
  )
  public PublicProductDetailResponse execute(String productId) {
    Product product = productRepository.findById(productId)
      .orElseThrow(() -> new ApiNotFoundException("Product not found: " + productId));
    return PublicProductDetailResponse.from(product, catalogAdminService.getProductDetail(productId));
  }
}
