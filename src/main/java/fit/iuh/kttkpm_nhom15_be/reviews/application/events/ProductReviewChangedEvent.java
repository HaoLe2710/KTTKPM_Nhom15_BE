package fit.iuh.kttkpm_nhom15_be.reviews.application.events;

import java.time.LocalDateTime;

public record ProductReviewChangedEvent(
  String productId,
  String reason,
  LocalDateTime sourceUpdatedAt
) {}
