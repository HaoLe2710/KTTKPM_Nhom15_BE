package fit.iuh.kttkpm_nhom15_be.orders.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fit.iuh.kttkpm_nhom15_be.orders.application.commands.QuoteShippingFeeCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.ShippingFeeQuoteDTO;
import fit.iuh.kttkpm_nhom15_be.orders.application.interfaces.ShippingFeeQuoteGateway;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingProvider;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuoteShippingFeeUseCaseTest {

  @Test
  void quoteShippingFeeRoutesToMatchingGateway() {
    ShippingFeeQuoteGateway gateway = mock(ShippingFeeQuoteGateway.class);
    ShippingFeeQuoteDTO expected = new ShippingFeeQuoteDTO(
      ShippingProvider.GHTK,
      BigDecimal.valueOf(18000),
      BigDecimal.ZERO,
      true,
      1000,
      "ok"
    );
    when(gateway.supportedProvider()).thenReturn(ShippingProvider.GHTK);
    when(gateway.quote(org.mockito.ArgumentMatchers.any())).thenReturn(expected);

    QuoteShippingFeeUseCase useCase = new QuoteShippingFeeUseCase(List.of(gateway));
    ShippingFeeQuoteDTO actual = useCase.execute(new QuoteShippingFeeCommand(
      ShippingProvider.GHTK,
      "123 Street",
      "Ho Chi Minh",
      "District 1",
      "Ben Nghe",
      BigDecimal.valueOf(200000),
      2
    ));

    assertEquals(expected, actual);
  }

  @Test
  void quoteShippingFeeFailsWhenProviderHasNoGateway() {
    QuoteShippingFeeUseCase useCase = new QuoteShippingFeeUseCase(List.of());

    assertThrows(
      ApiValidationException.class,
      () -> useCase.execute(new QuoteShippingFeeCommand(
        ShippingProvider.GHTK,
        null,
        "Ho Chi Minh",
        "District 1",
        null,
        BigDecimal.valueOf(200000),
        2
      ))
    );
  }
}
