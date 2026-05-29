package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.cache;

import fit.iuh.kttkpm_nhom15_be.catalog.application.events.CatalogProductChangedEvent;
import fit.iuh.kttkpm_nhom15_be.orders.application.events.ProductSalesChangedEvent;
import fit.iuh.kttkpm_nhom15_be.reviews.application.events.ProductReviewChangedEvent;
import fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheKeys;
import fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCacheInvalidationListener {

  private final CacheManager cacheManager;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onCatalogProductChanged(CatalogProductChangedEvent event) {
    evictProductCaches(event.productId(), event.reason());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onProductSalesChanged(ProductSalesChangedEvent event) {
    if (event.productIds() != null) {
      event.productIds().forEach(this::evictProductDetail);
    }
    clearSharedProductCaches(event.reason());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onProductReviewChanged(ProductReviewChangedEvent event) {
    evictProductCaches(event.productId(), event.reason());
  }

  private void evictProductCaches(String productId, String reason) {
    evictProductDetail(productId);
    clearSharedProductCaches(reason);
  }

  private void evictProductDetail(String productId) {
    if (productId == null || productId.isBlank()) {
      clear(CacheNames.PRODUCT_DETAIL);
      return;
    }
    Cache detailCache = cacheManager.getCache(CacheNames.PRODUCT_DETAIL);
    if (detailCache != null) {
      detailCache.evict(CacheKeys.productDetail(productId));
      log.info("Evicted product detail cache for productId={}", productId);
    }
  }

  private void clearSharedProductCaches(String reason) {
    clear(CacheNames.PRODUCT_LIST);
    clear(CacheNames.PRODUCT_SEARCH);
    clear(CacheNames.SEARCH_SUGGESTIONS);
    clear(CacheNames.PRODUCT_MASTER_DATA);
    log.info("Cleared product cache groups after product data change, reason={}", reason);
  }

  private void clear(String cacheName) {
    Cache cache = cacheManager.getCache(cacheName);
    if (cache != null) {
      cache.clear();
    }
  }
}
