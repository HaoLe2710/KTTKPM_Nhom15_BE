package fit.iuh.kttkpm_nhom15_be.search.application.usecases.projection;

import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchProjectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconcileSearchProjectionsUseCase {

  private final SearchProjectionRepository searchProjectionRepository;

  public void execute() {
    String runId = searchProjectionRepository.startProjectionRun("RECONCILIATION");
    int processed = 0;
    String cursor = null;

    try {
      for (String productId : searchProjectionRepository.findStaleOrMissingProductIds(200)) {
        searchProjectionRepository.enqueueProjectionTask(productId, "RECONCILIATION");
        processed++;
        cursor = productId;
      }
      searchProjectionRepository.finishProjectionRun(runId, "COMPLETED", cursor, processed, 0);
    } catch (Exception ex) {
      searchProjectionRepository.finishProjectionRun(runId, "FAILED", cursor, processed, 1);
      log.error("Projection reconciliation failed", ex);
    }
  }
}
