package fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions;

public class UnauthorizedReviewAccessException extends RuntimeException {
    public UnauthorizedReviewAccessException(String reviewId, String userId) {
        super("Bạn không có quyền xóa đánh giá này. (Review: " + reviewId + ", User: " + userId + ")");
    }
}
