package fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions;

public class ReviewAlreadyExistsException extends RuntimeException {
    public ReviewAlreadyExistsException(String userId, String productId) {
        super("Bạn đã đánh giá sản phẩm này rồi. (User: " + userId + ", Product: " + productId + ")");
    }
}
