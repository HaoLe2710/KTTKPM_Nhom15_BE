package fit.iuh.kttkpm_nhom15_be.orders.infrastructure.notification;

import fit.iuh.kttkpm_nhom15_be.orders.application.events.OrderCancelledEvent;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.notification.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancellationEmailListener {

  private final EmailService emailService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void sendOrderCancellation(OrderCancelledEvent event) {
    if (event.recipientEmail() == null || event.recipientEmail().isBlank()) {
      log.warn("Order {} has no recipient email, skipping cancellation email", event.orderNo());
      return;
    }

    try {
      emailService.sendOrderCancellationEmail(new EmailService.OrderCancellationEmail(
        event.recipientEmail(),
        event.recipientName(),
        event.recipientPhone(),
        event.orderNo(),
        event.shippingAddress(),
        event.paymentMethod(),
        event.totalAmount(),
        event.reason()
      ));
    } catch (RuntimeException ex) {
      log.error("Order cancellation email failed for order {}: {}", event.orderNo(), ex.getMessage());
    }
  }
}
