package fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String reviewId) {
        super("Đánh giá không tồn tại: " + reviewId);
    }
}
