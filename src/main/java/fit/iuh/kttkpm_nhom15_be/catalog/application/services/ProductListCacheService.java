package fit.iuh.kttkpm_nhom15_be.catalog.application.services;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.ProductSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.usecases.query.BrowseLegacyProductsUseCase;
import fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheNames;
import fit.iuh.kttkpm_nhom15_be.shared.application.cache.CachedPage;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductListCacheService {

  private final BrowseLegacyProductsUseCase browseLegacyProductsUseCase;

  @Cacheable(
    cacheNames = CacheNames.PRODUCT_LIST,
    key = "T(fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheKeys).productList(#typeId, #minPrice, #maxPrice, #page, #size)"
  )
  public CachedPage<ProductSummaryDTO> getProductSummaries(String typeId,
                                                           BigDecimal minPrice,
                                                           BigDecimal maxPrice,
                                                           int page,
                                                           int size) {
    return CachedPage.from(browseLegacyProductsUseCase.execute(typeId, minPrice, maxPrice, page, size));
  }
}
