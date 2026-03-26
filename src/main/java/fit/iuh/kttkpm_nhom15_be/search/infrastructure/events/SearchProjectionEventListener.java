package fit.iuh.kttkpm_nhom15_be.search.infrastructure.events;

import fit.iuh.kttkpm_nhom15_be.catalog.application.events.CatalogProductChangedEvent;
import fit.iuh.kttkpm_nhom15_be.orders.application.events.ProductSalesChangedEvent;
import fit.iuh.kttkpm_nhom15_be.reviews.application.events.ProductReviewChangedEvent;
import fit.iuh.kttkpm_nhom15_be.search.application.usecases.projection.EnqueueSearchProjectionUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SearchProjectionEventListener {

  private final EnqueueSearchProjectionUseCase enqueueSearchProjectionUseCase;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onCatalogProductChanged(CatalogProductChangedEvent event) {
    enqueueSearchProjectionUseCase.execute(event.productId(), event.reason());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onProductSalesChanged(ProductSalesChangedEvent event) {
    event.productIds().forEach(productId -> enqueueSearchProjectionUseCase.execute(productId, event.reason()));
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onProductReviewChanged(ProductReviewChangedEvent event) {
    enqueueSearchProjectionUseCase.execute(event.productId(), event.reason());
  }
}
