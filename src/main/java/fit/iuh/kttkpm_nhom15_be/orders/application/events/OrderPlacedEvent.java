package fit.iuh.kttkpm_nhom15_be.orders.application.events;

import java.math.BigDecimal;

/**
 * Spring Application Event được publish sau khi đặt hàng thành công.
 * Các module khác (payments, notifications) có thể subscribe event này
 * thông qua @EventListener hoặc @TransactionalEventListener.
 */
public record OrderPlacedEvent(
  String orderId,
  String orderNo,
  String userId,
  BigDecimal totalAmount
) {}
