package fit.iuh.kttkpm_nhom15_be.reviews.application.dto;

import java.time.LocalDateTime;

public record ProductReviewDTO(
    String id,
    String reviewerName,
    int rating,
    String content,
    LocalDateTime createdAt
) {}
