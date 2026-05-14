package fit.iuh.kttkpm_nhom15_be.orders.domain.states;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;

public interface OrderState {
  void confirm(Order order);
  void cancel(Order order, String reason);
  void ship(Order order);
  void complete(Order order);
}