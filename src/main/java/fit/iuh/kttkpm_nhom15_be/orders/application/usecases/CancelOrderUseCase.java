package fit.iuh.kttkpm_nhom15_be.orders.application.usecases;

import fit.iuh.kttkpm_nhom15_be.catalog.application.interfaces.CatalogFacade;
import fit.iuh.kttkpm_nhom15_be.orders.application.commands.CancelOrderCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.StockRestoreItem;
import fit.iuh.kttkpm_nhom15_be.orders.application.events.OrderCancelledEvent;
import fit.iuh.kttkpm_nhom15_be.orders.application.results.CancelOrderResult;
import fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions.OrderNotFoundException;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CancelOrderUseCase {

  private final OrderRepository orderRepository;
  private final CatalogFacade catalogFacade;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public CancelOrderResult execute(CancelOrderCommand command) {
    // 1. Tìm đơn hàng theo ID
    Order order = orderRepository.findById(command.orderId())
      .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

    // 2. Áp dụng State Pattern — ủy quyền sang state hiện tại
    //    CreatedState/ConfirmedState → chuyển sang CANCELLED
    //    ShippingState/CompletedState/CancelledState → throw InvalidOrderStateTransitionException
    order.cancelOrder(command.reason());

    // 3. Giao tiếp đồng bộ: hoàn lại số lượng đã trừ
    List<StockRestoreItem> restoreItems = order.getItems().stream()
      .map(item -> new StockRestoreItem(item.getVariantId(), item.getQuantity()))
      .toList();
    catalogFacade.restoreStock(restoreItems);

    // 4. Lưu đơn hàng đã cập nhật vào DB
    Order savedOrder = orderRepository.save(order);

    // 5. Publish domain event — kích hoạt Payment refund + Notification email
    eventPublisher.publishEvent(new OrderCancelledEvent(
      savedOrder.getId(),
      savedOrder.getOrderNo(),
      savedOrder.getUserId(),
      savedOrder.getTotalAmount(),
      command.reason()
    ));

    // 6. Trả về kết quả
    return CancelOrderResult.builder()
      .orderId(savedOrder.getId())
      .orderNo(savedOrder.getOrderNo())
      .status(savedOrder.getStatus().name())
      .build();
  }
}
