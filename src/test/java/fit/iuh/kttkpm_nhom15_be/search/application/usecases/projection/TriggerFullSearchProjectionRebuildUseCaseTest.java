package fit.iuh.kttkpm_nhom15_be.search.application.usecases.projection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchProjectionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

class TriggerFullSearchProjectionRebuildUseCaseTest {

  @Test
  void executeQueuesAllActiveProductsInBatchesAndFinishesRun() {
    SearchProjectionRepository searchProjectionRepository = Mockito.mock(SearchProjectionRepository.class);
    TriggerFullSearchProjectionRebuildUseCase useCase = new TriggerFullSearchProjectionRebuildUseCase(searchProjectionRepository);

    when(searchProjectionRepository.startProjectionRun("FULL_REBUILD")).thenReturn("run-1");
    when(searchProjectionRepository.findActiveProductIdsAfter(null, 200)).thenReturn(List.of("product-1", "product-2"));
    when(searchProjectionRepository.findActiveProductIdsAfter("product-2", 200)).thenReturn(List.of("product-3"));
    when(searchProjectionRepository.findActiveProductIdsAfter("product-3", 200)).thenReturn(List.of());

    var result = useCase.execute();

    assertEquals("run-1", result.runId());
    assertEquals("QUEUED", result.status());
    assertEquals(3, result.queuedProducts());

    InOrder inOrder = Mockito.inOrder(searchProjectionRepository);
    inOrder.verify(searchProjectionRepository).startProjectionRun("FULL_REBUILD");
    inOrder.verify(searchProjectionRepository).findActiveProductIdsAfter(null, 200);
    inOrder.verify(searchProjectionRepository).enqueueProjectionTask("product-1", "FULL_REBUILD");
    inOrder.verify(searchProjectionRepository).enqueueProjectionTask("product-2", "FULL_REBUILD");
    inOrder.verify(searchProjectionRepository).findActiveProductIdsAfter("product-2", 200);
    inOrder.verify(searchProjectionRepository).enqueueProjectionTask("product-3", "FULL_REBUILD");
    inOrder.verify(searchProjectionRepository).findActiveProductIdsAfter("product-3", 200);
    inOrder.verify(searchProjectionRepository).finishProjectionRun("run-1", "QUEUED", "product-3", 3, 0);

    verify(searchProjectionRepository).enqueueProjectionTask("product-1", "FULL_REBUILD");
    verify(searchProjectionRepository).enqueueProjectionTask("product-2", "FULL_REBUILD");
    verify(searchProjectionRepository).enqueueProjectionTask("product-3", "FULL_REBUILD");
  }
}
