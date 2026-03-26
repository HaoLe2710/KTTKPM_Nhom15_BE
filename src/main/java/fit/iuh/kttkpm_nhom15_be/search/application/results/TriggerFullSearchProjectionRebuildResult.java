package fit.iuh.kttkpm_nhom15_be.search.application.results;

public record TriggerFullSearchProjectionRebuildResult(
  String runId,
  String status,
  int queuedProducts
) {}
