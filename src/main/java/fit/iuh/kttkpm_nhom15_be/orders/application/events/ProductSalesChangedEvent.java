package fit.iuh.kttkpm_nhom15_be.orders.application.events;

import java.time.LocalDateTime;
import java.util.List;

public record ProductSalesChangedEvent(
  List<String> productIds,
  String reason,
  LocalDateTime sourceUpdatedAt
) {}
