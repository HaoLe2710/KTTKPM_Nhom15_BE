package fit.iuh.kttkpm_nhom15_be.search.application.usecases.projection;

import fit.iuh.kttkpm_nhom15_be.search.domain.models.SearchProjectionTask;
import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchProjectionRepository;
import java.time.Duration;
import java.util.List;
import java.util.random.RandomGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessSearchProjectionTasksUseCase {

  private static final List<Duration> RETRY_DELAYS = List.of(
    Duration.ofMinutes(1),
    Duration.ofMinutes(5),
    Duration.ofMinutes(15),
    Duration.ofHours(1),
    Duration.ofHours(6),
    Duration.ofHours(24)
  );

  private final SearchProjectionRepository searchProjectionRepository;
  private final RandomGenerator random = RandomGenerator.getDefault();

  public void execute() {
    for (SearchProjectionTask task : searchProjectionRepository.findDueProjectionTasks(25)) {
      searchProjectionRepository.markTaskProcessing(task.productId());
      try {
        searchProjectionRepository.rebuildProductProjection(task.productId());
        searchProjectionRepository.markTaskSucceeded(task.productId());
      } catch (Exception ex) {
        int nextAttempt = task.attemptCount() + 1;
        if (nextAttempt > RETRY_DELAYS.size()) {
          searchProjectionRepository.markTaskPermanentFailure(task.productId(), nextAttempt, task.reason(), ex.getMessage());
          log.error("Projection failed permanently for productId={}", task.productId(), ex);
          continue;
        }

        searchProjectionRepository.markTaskRetry(task.productId(), nextAttempt, jitter(RETRY_DELAYS.get(nextAttempt - 1)), ex.getMessage());
        log.warn("Projection retry scheduled for productId={}, attempt={}", task.productId(), nextAttempt, ex);
      }
    }
  }

  private Duration jitter(Duration delay) {
    long millis = delay.toMillis();
    long jitter = Math.round(millis * 0.1d);
    long offset = jitter == 0 ? 0 : random.nextLong(-jitter, jitter + 1);
    return Duration.ofMillis(Math.max(1000L, millis + offset));
  }
}
