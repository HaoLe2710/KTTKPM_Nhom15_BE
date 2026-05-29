package fit.iuh.kttkpm_nhom15_be.orders.application.commands;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingProvider;
import java.math.BigDecimal;

public record QuoteShippingFeeCommand(
  ShippingProvider shippingProvider,
  String shipAddress,
  String shipCity,
  String shipDistrict,
  String shipWard,
  BigDecimal orderValue,
  int itemQuantity
) {}
