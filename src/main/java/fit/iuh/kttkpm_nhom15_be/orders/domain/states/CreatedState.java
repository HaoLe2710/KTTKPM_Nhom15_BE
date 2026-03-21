package fit.iuh.kttkpm_nhom15_be.orders.domain.states;

import fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions.InvalidOrderStateTransitionException;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;

/**
 * CREATED state: đơn hàng vừa được đặt, chưa xác nhận.
 * confirm() → CONFIRMED
 * cancel()  → CANCELLED
 * ship() / complete() → throw
 */
public class CreatedState implements OrderState {

  @Override
  public void confirm(Order order) {
    order.setStatus(OrderStatus.CONFIRMED);
    order.setState(new ConfirmedState());
  }

  @Override
  public void cancel(Order order, String reason) {
    order.setStatus(OrderStatus.CANCELLED);
    order.setState(new CancelledState());
  }

  @Override
  public void ship(Order order) {
    throw new InvalidOrderStateTransitionException("CREATED", "ship — phải xác nhận trước khi giao");
  }

  @Override
  public void complete(Order order) {
    throw new InvalidOrderStateTransitionException("CREATED", "complete");
  }
}