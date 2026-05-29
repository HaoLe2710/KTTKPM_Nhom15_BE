package fit.iuh.kttkpm_nhom15_be.reviews.application.dto;

public record ReviewEligibilityDTO(
    boolean canReview,
    String orderId,
    boolean alreadyReviewed,
    String message
) {}
