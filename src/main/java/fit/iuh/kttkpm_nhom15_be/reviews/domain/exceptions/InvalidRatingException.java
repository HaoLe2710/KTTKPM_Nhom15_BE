package fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions;

public class InvalidRatingException extends RuntimeException {
    public InvalidRatingException(int rating) {
        super("Điểm đánh giá phải từ 1 đến 5. Nhận được: " + rating);
    }
}
