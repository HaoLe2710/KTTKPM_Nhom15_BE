package fit.iuh.kttkpm_nhom15_be.search.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.search.domain.models.SearchProjectionTask;
import java.time.Duration;
import java.util.List;

public interface SearchProjectionRepository {

  void enqueueProjectionTask(String productId, String reason);

  List<SearchProjectionTask> findDueProjectionTasks(int limit);

  void markTaskProcessing(String productId);

  void markTaskSucceeded(String productId);

  void markTaskRetry(String productId, int attemptCount, Duration delay, String errorMessage);

  void markTaskPermanentFailure(String productId, int attemptCount, String eventType, String errorMessage);

  String startProjectionRun(String runType);

  void finishProjectionRun(String runId, String status, String cursorProductId, int processedCount, int failedCount);

  List<String> findActiveProductIdsAfter(String cursorProductId, int limit);

  List<String> findStaleOrMissingProductIds(int limit);

  void rebuildProductProjection(String productId);
}
