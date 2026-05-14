package fit.iuh.kttkpm_nhom15_be.orders.application.events;

import java.math.BigDecimal;

/**
 * Spring Application Event được publish sau khi hủy đơn hàng thành công.
 * Các module khác (payments → xử lý refund, notifications → gửi email hủy)
 * có thể subscribe event này thông qua @EventListener hoặc @TransactionalEventListener.
 */
public record OrderCancelledEvent(
  String orderId,
  String orderNo,
  String userId,
  BigDecimal totalAmount,
  String reason
) {}
