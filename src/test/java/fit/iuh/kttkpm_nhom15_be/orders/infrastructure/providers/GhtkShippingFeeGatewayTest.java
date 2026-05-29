package fit.iuh.kttkpm_nhom15_be.orders.infrastructure.providers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import fit.iuh.kttkpm_nhom15_be.orders.application.commands.QuoteShippingFeeCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.ShippingFeeQuoteDTO;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingProvider;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

class GhtkShippingFeeGatewayTest {

  @Test
  void quoteUsesHcmFallbackWhenConfigurationIsIncomplete() {
    GhtkProperties properties = new GhtkProperties();
    properties.setToken("");
    properties.setClientSource("");
    properties.setPickProvince("TP. Ho Chi Minh");
    properties.setPickDistrict("Quan 1");

    GhtkShippingFeeGateway gateway = new GhtkShippingFeeGateway(properties, new RestTemplateBuilder());
    ShippingFeeQuoteDTO quote = gateway.quote(new QuoteShippingFeeCommand(
      ShippingProvider.GHTK,
      "123 Lê Lợi",
      "TP. Ho Chi Minh",
      "Quan 1",
      "Ben Nghe",
      BigDecimal.valueOf(200000),
      2
    ));

    assertEquals(BigDecimal.valueOf(15000), quote.fee());
    assertEquals(true, quote.deliverySupported());
    assertEquals(1000, quote.weightGrams());
  }

  @Test
  void quoteUsesOtherProvinceFallbackWhenConfigurationIsIncomplete() {
    GhtkProperties properties = new GhtkProperties();
    properties.setToken("");
    properties.setClientSource("");
    properties.setPickProvince("TP. Ho Chi Minh");
    properties.setPickDistrict("Quan 1");

    GhtkShippingFeeGateway gateway = new GhtkShippingFeeGateway(properties, new RestTemplateBuilder());
    ShippingFeeQuoteDTO quote = gateway.quote(new QuoteShippingFeeCommand(
      ShippingProvider.GHTK,
      "45 Nguyen Hue",
      "Da Nang",
      "Hai Chau",
      "Hai Chau 1",
      BigDecimal.valueOf(200000),
      3
    ));

    assertEquals(BigDecimal.valueOf(30000), quote.fee());
    assertEquals(true, quote.deliverySupported());
    assertEquals(1500, quote.weightGrams());
  }
}
