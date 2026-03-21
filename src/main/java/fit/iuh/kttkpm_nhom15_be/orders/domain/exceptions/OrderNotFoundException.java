package fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions;

public class OrderNotFoundException extends RuntimeException {

  public OrderNotFoundException(String orderId) {
    super("Không tìm thấy đơn hàng với ID: " + orderId);
  }
}
