package fit.iuh.kttkpm_nhom15_be.orders.application.commands;

/**
 * Command DTO cho use case hủy đơn hàng.
 */
public record CancelOrderCommand(
  String orderId,
  String userId,
  String reason
) {}
