package fit.iuh.kttkpm_nhom15_be.carts.application.commands;

public record AddToCartCommand(
  String userId,
  String variantId,
  int quantity
) {}
