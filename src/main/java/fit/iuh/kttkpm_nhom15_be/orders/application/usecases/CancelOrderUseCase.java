package fit.iuh.kttkpm_nhom15_be.orders.application.usecases;

import fit.iuh.kttkpm_nhom15_be.catalog.application.interfaces.CatalogFacade;
import fit.iuh.kttkpm_nhom15_be.orders.application.commands.CancelOrderCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.StockRestoreItem;
import fit.iuh.kttkpm_nhom15_be.orders.application.events.OrderCancelledEvent;
import fit.iuh.kttkpm_nhom15_be.orders.application.events.ProductSalesChangedEvent;
import fit.iuh.kttkpm_nhom15_be.orders.application.results.CancelOrderResult;
import fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions.OrderNotFoundException;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CancelOrderUseCase {

  private final OrderRepository orderRepository;
  private final CatalogFacade catalogFacade;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public CancelOrderResult execute(CancelOrderCommand command) {
    Order order = orderRepository.findById(command.orderId())
      .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

    order.cancelOrder(command.reason());

    if (order.isStockDeducted()) {
      List<StockRestoreItem> restoreItems = order.getItems().stream()
        .map(item -> new StockRestoreItem(item.getVariantId(), item.getQuantity()))
        .toList();
      catalogFacade.restoreStock(restoreItems);
      order.setStockDeducted(false);
    }

    Order savedOrder = orderRepository.save(order);

    eventPublisher.publishEvent(new OrderCancelledEvent(
      savedOrder.getId(),
      savedOrder.getOrderNo(),
      savedOrder.getUserId(),
      savedOrder.getTotalAmount(),
      command.reason()
    ));
    eventPublisher.publishEvent(new ProductSalesChangedEvent(
      savedOrder.getItems().stream()
        .map(item -> item.getProductId())
        .filter(productId -> productId != null && !productId.isBlank())
        .distinct()
        .toList(),
      "ORDER_CANCELLED",
      LocalDateTime.now()
    ));

    return CancelOrderResult.builder()
      .orderId(savedOrder.getId())
      .orderNo(savedOrder.getOrderNo())
      .status(savedOrder.getStatus().name())
      .build();
  }
}
