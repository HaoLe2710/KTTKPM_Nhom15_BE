package fit.iuh.kttkpm_nhom15_be.orders.domain.states;

import fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions.InvalidOrderStateTransitionException;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;

/**
 * SHIPPING state: package is in transit.
 * Allowed: complete() → COMPLETED
 * Forbidden: confirm(), cancel() (cannot cancel an in-transit order)
 */
public class ShippingState implements OrderState {

  @Override
  public void confirm(Order order) {
    throw new InvalidOrderStateTransitionException("SHIPPING", "confirm");
  }

  @Override
  public void cancel(Order order, String reason) {
    throw new InvalidOrderStateTransitionException("SHIPPING", "cancel — đơn hàng đang trên đường giao, không thể hủy");
  }

  @Override
  public void ship(Order order) {
    throw new InvalidOrderStateTransitionException("SHIPPING", "ship");
  }

  @Override
  public void complete(Order order) {
    order.setStatus(OrderStatus.COMPLETED);
    order.setState(new CompletedState());
  }
}
