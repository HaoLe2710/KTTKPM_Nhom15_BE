package fit.iuh.kttkpm_nhom15_be.orders.infrastructure.notification;

import fit.iuh.kttkpm_nhom15_be.orders.application.events.OrderPlacedEvent;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.notification.EmailService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConfirmationEmailListener {

  private final EmailService emailService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void sendOrderConfirmation(OrderPlacedEvent event) {
    if (event.recipientEmail() == null || event.recipientEmail().isBlank()) {
      log.warn("Order {} has no recipient email, skipping confirmation email", event.orderNo());
      return;
    }

    try {
      emailService.sendOrderConfirmationEmail(new EmailService.OrderConfirmationEmail(
        event.recipientEmail(),
        event.recipientName(),
        event.recipientPhone(),
        event.orderNo(),
        event.shippingAddress(),
        event.paymentMethod(),
        event.subtotalAmount(),
        event.discountAmount(),
        event.shippingFee(),
        event.totalAmount(),
        toEmailItems(event.items())
      ));
    } catch (RuntimeException ex) {
      log.error("Order confirmation email failed for order {}: {}", event.orderNo(), ex.getMessage());
    }
  }

  private List<EmailService.OrderConfirmationItem> toEmailItems(List<OrderPlacedEvent.Item> items) {
    if (items == null) {
      return List.of();
    }

    return items.stream()
      .map(item -> new EmailService.OrderConfirmationItem(
        item.name(),
        item.sku(),
        item.quantity(),
        item.unitPrice(),
        item.lineTotal()
      ))
      .toList();
  }
}
