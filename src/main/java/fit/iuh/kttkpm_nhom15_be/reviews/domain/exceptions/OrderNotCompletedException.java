package fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions;

public class OrderNotCompletedException extends RuntimeException {
    public OrderNotCompletedException(String orderId) {
        super("Chỉ có thể đánh giá sản phẩm từ đơn hàng đã hoàn thành. (Order: " + orderId + ")");
    }
}
