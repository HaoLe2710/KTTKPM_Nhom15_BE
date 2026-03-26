package fit.iuh.kttkpm_nhom15_be.search.infrastructure.scheduling;

import fit.iuh.kttkpm_nhom15_be.search.application.usecases.projection.ProcessSearchProjectionTasksUseCase;
import fit.iuh.kttkpm_nhom15_be.search.application.usecases.projection.ReconcileSearchProjectionsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchProjectionScheduler {

  private final ProcessSearchProjectionTasksUseCase processSearchProjectionTasksUseCase;
  private final ReconcileSearchProjectionsUseCase reconcileSearchProjectionsUseCase;

  @Scheduled(fixedDelay = 30000)
  public void processDueTasks() {
    processSearchProjectionTasksUseCase.execute();
  }

  @Scheduled(fixedDelay = 900000)
  public void reconcileMissingOrStaleProjections() {
    reconcileSearchProjectionsUseCase.execute();
  }
}
