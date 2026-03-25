package fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderId) {
        super("Đơn hàng không tồn tại: " + orderId);
    }
}
