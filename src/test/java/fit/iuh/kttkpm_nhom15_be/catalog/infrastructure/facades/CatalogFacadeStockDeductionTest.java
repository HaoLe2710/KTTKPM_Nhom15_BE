package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.facades;

import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartItemDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.exceptions.ProductUnavailableException;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.MediaRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionValueRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantRepository;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CatalogFacadeStockDeductionTest {

  @Test
  void deductStockAllowsOnlyOneConcurrentCheckoutWhenOnlyOneUnitAvailable() throws Exception {
    VariantRepository variantRepository = Mockito.mock(VariantRepository.class);
    AtomicInteger remainingStock = new AtomicInteger(1);
    when(variantRepository.deductStock(eq("variant-1"), eq(1))).thenAnswer(invocation -> {
      int requestedQuantity = invocation.getArgument(1, Integer.class);
      int previousStock = remainingStock.getAndUpdate(currentStock ->
        currentStock >= requestedQuantity ? currentStock - requestedQuantity : currentStock
      );
      return previousStock >= requestedQuantity;
    });

    CatalogFacadeImpl facade = new CatalogFacadeImpl(
      variantRepository,
      Mockito.mock(ProductRepository.class),
      Mockito.mock(MediaRepository.class),
      Mockito.mock(OptionValueRepository.class),
      Mockito.mock(OptionRepository.class)
    );
    CartItemDTO item = CartItemDTO.builder()
      .variantId("variant-1")
      .quantity(1)
      .build();

    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);

    try {
      var firstAttempt = executorService.submit(() -> attemptDeductStock(facade, item, ready, start));
      var secondAttempt = executorService.submit(() -> attemptDeductStock(facade, item, ready, start));

      assertTrue(ready.await(1, TimeUnit.SECONDS));
      start.countDown();

      List<Boolean> results = List.of(
        firstAttempt.get(2, TimeUnit.SECONDS),
        secondAttempt.get(2, TimeUnit.SECONDS)
      );

      assertEquals(1, results.stream().filter(Boolean::booleanValue).count());
      assertEquals(0, remainingStock.get());
    } finally {
      executorService.shutdownNow();
    }
  }

  private boolean attemptDeductStock(CatalogFacadeImpl facade,
                                     CartItemDTO item,
                                     CountDownLatch ready,
                                     CountDownLatch start) throws InterruptedException {
    ready.countDown();
    assertTrue(start.await(1, TimeUnit.SECONDS));

    try {
      facade.deductStock(List.of(item));
      return true;
    } catch (ProductUnavailableException ex) {
      return false;
    }
  }
}
