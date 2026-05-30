package fit.iuh.kttkpm_nhom15_be.orders.application.events;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Application Event được publish sau khi đặt hàng thành công.
 * Các module khác (payments, notifications) có thể subscribe event này
 * thông qua @EventListener hoặc @TransactionalEventListener.
 */
public record OrderPlacedEvent(
  String orderId,
  String orderNo,
  String userId,
  String recipientEmail,
  String recipientName,
  String recipientPhone,
  String shippingAddress,
  String paymentMethod,
  BigDecimal subtotalAmount,
  BigDecimal discountAmount,
  BigDecimal shippingFee,
  BigDecimal totalAmount,
  List<Item> items
) {
  public record Item(
    String name,
    String sku,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal
  ) {}
}
