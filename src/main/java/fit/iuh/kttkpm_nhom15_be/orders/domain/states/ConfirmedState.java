package fit.iuh.kttkpm_nhom15_be.orders.domain.states;

import fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions.InvalidOrderStateTransitionException;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;

/**
 * CONFIRMED state: đơn hàng đã xác nhận (shop gọi xác nhận COD hoặc đã thanh toán online).
 * ship()   → SHIPPING
 * cancel() → CANCELLED
 * confirm() / complete() → throw
 */
public class ConfirmedState implements OrderState {

  @Override
  public void confirm(Order order) {
    throw new InvalidOrderStateTransitionException("CONFIRMED", "confirm — đơn hàng đã được xác nhận trước đó");
  }

  @Override
  public void cancel(Order order, String reason) {
    order.setStatus(OrderStatus.CANCELLED);
    order.setState(new CancelledState());
  }

  @Override
  public void ship(Order order) {
    order.setStatus(OrderStatus.SHIPPING);
    order.setState(new ShippingState());
  }

  @Override
  public void complete(Order order) {
    throw new InvalidOrderStateTransitionException("CONFIRMED", "complete — phải giao hàng trước");
  }
}