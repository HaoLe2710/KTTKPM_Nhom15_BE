package fit.iuh.kttkpm_nhom15_be.orders.domain.states;

import fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions.InvalidOrderStateTransitionException;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;

/**
 * Terminal state — no further transitions allowed.
 */
public class CancelledState implements OrderState {

  @Override
  public void confirm(Order order) {
    throw new InvalidOrderStateTransitionException("CANCELLED", "confirm");
  }

  @Override
  public void cancel(Order order, String reason) {
    throw new InvalidOrderStateTransitionException("CANCELLED", "cancel");
  }

  @Override
  public void ship(Order order) {
    throw new InvalidOrderStateTransitionException("CANCELLED", "ship");
  }

  @Override
  public void complete(Order order) {
    throw new InvalidOrderStateTransitionException("CANCELLED", "complete");
  }
}
