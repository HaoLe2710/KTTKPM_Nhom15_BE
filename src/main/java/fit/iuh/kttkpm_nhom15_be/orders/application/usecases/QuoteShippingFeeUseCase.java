package fit.iuh.kttkpm_nhom15_be.orders.application.usecases;

import fit.iuh.kttkpm_nhom15_be.orders.application.commands.QuoteShippingFeeCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.ShippingFeeQuoteDTO;
import fit.iuh.kttkpm_nhom15_be.orders.application.interfaces.ShippingFeeQuoteGateway;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingProvider;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class QuoteShippingFeeUseCase {

  private final Map<ShippingProvider, ShippingFeeQuoteGateway> gateways;

  public QuoteShippingFeeUseCase(List<ShippingFeeQuoteGateway> gateways) {
    this.gateways = new EnumMap<>(ShippingProvider.class);
    gateways.forEach(gateway -> this.gateways.put(gateway.supportedProvider(), gateway));
  }

  public ShippingFeeQuoteDTO execute(QuoteShippingFeeCommand command) {
    if (command.shippingProvider() == null) {
      throw new ApiValidationException("shippingProvider không được để trống.");
    }

    ShippingFeeQuoteGateway gateway = gateways.get(command.shippingProvider());
    if (gateway == null) {
      throw new ApiValidationException("Chưa hỗ trợ tính phí vận chuyển cho đơn vị " + command.shippingProvider() + ".");
    }

    return gateway.quote(command);
  }
}
