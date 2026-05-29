package fit.iuh.kttkpm_nhom15_be.orders.application.interfaces;

import fit.iuh.kttkpm_nhom15_be.orders.application.commands.QuoteShippingFeeCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.ShippingFeeQuoteDTO;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingProvider;

public interface ShippingFeeQuoteGateway {

  ShippingProvider supportedProvider();

  ShippingFeeQuoteDTO quote(QuoteShippingFeeCommand command);
}
