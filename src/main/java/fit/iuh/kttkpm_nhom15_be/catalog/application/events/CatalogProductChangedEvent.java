package fit.iuh.kttkpm_nhom15_be.catalog.application.events;

import java.time.LocalDateTime;

public record CatalogProductChangedEvent(
  String productId,
  String reason,
  LocalDateTime sourceUpdatedAt
) {}
