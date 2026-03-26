package fit.iuh.kttkpm_nhom15_be.search.application.usecases.projection;

import fit.iuh.kttkpm_nhom15_be.search.application.results.TriggerFullSearchProjectionRebuildResult;
import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchProjectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerFullSearchProjectionRebuildUseCase {

  private static final int BATCH_SIZE = 200;
  private static final String RUN_TYPE = "FULL_REBUILD";

  private final SearchProjectionRepository searchProjectionRepository;

  public TriggerFullSearchProjectionRebuildResult execute() {
    String runId = searchProjectionRepository.startProjectionRun(RUN_TYPE);
    int queuedProducts = 0;
    String cursor = null;

    try {
      while (true) {
        var productIds = searchProjectionRepository.findActiveProductIdsAfter(cursor, BATCH_SIZE);
        if (productIds.isEmpty()) {
          break;
        }

        for (String productId : productIds) {
          searchProjectionRepository.enqueueProjectionTask(productId, RUN_TYPE);
          queuedProducts++;
          cursor = productId;
        }
      }

      searchProjectionRepository.finishProjectionRun(runId, "QUEUED", cursor, queuedProducts, 0);
      return new TriggerFullSearchProjectionRebuildResult(runId, "QUEUED", queuedProducts);
    } catch (Exception ex) {
      searchProjectionRepository.finishProjectionRun(runId, "FAILED", cursor, queuedProducts, 1);
      log.error("Full search projection rebuild enqueue failed for runId={}", runId, ex);
      throw ex;
    }
  }
}
