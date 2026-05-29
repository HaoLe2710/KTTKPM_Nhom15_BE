package fit.iuh.kttkpm_nhom15_be.orders.application.dto;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingProvider;
import java.math.BigDecimal;

public record ShippingFeeQuoteDTO(
  ShippingProvider shippingProvider,
  BigDecimal fee,
  BigDecimal insuranceFee,
  boolean deliverySupported,
  int weightGrams,
  String message
) {}
