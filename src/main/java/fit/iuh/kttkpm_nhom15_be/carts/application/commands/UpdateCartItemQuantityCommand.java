package fit.iuh.kttkpm_nhom15_be.carts.application.commands;

public record UpdateCartItemQuantityCommand(
  String userId,
  String variantId,
  int quantity
) {}
