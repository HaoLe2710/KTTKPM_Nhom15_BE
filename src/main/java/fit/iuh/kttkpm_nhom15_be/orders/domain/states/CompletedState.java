package fit.iuh.kttkpm_nhom15_be.orders.domain.states;

import fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions.InvalidOrderStateTransitionException;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;

/**
 * Terminal state — order has been successfully delivered.
 * No further transitions allowed.
 */
public class CompletedState implements OrderState {

  @Override
  public void confirm(Order order) {
    throw new InvalidOrderStateTransitionException("COMPLETED", "confirm");
  }

  @Override
  public void cancel(Order order, String reason) {
    throw new InvalidOrderStateTransitionException("COMPLETED", "cancel");
  }

  @Override
  public void ship(Order order) {
    throw new InvalidOrderStateTransitionException("COMPLETED", "ship");
  }

  @Override
  public void complete(Order order) {
    throw new InvalidOrderStateTransitionException("COMPLETED", "complete");
  }
}
