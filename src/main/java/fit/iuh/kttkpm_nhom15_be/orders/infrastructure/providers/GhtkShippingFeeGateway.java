package fit.iuh.kttkpm_nhom15_be.orders.infrastructure.providers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import fit.iuh.kttkpm_nhom15_be.orders.application.commands.QuoteShippingFeeCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.ShippingFeeQuoteDTO;
import fit.iuh.kttkpm_nhom15_be.orders.application.interfaces.ShippingFeeQuoteGateway;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingProvider;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.List;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GhtkShippingFeeGateway implements ShippingFeeQuoteGateway {

  private final GhtkProperties properties;
  private final RestTemplate restTemplate;

  public GhtkShippingFeeGateway(GhtkProperties properties, RestTemplateBuilder restTemplateBuilder) {
    this.properties = properties;
    this.restTemplate = restTemplateBuilder.build();
  }

  @Override
  public ShippingProvider supportedProvider() {
    return ShippingProvider.GHTK;
  }

  @Override
  public ShippingFeeQuoteDTO quote(QuoteShippingFeeCommand command) {
    int weightGrams = Math.max(command.itemQuantity(), 1) * properties.getDefaultWeightPerItemGrams();
    if (!hasValidConfiguration()) {
      return buildFallbackQuote(command, weightGrams, "Cấu hình GHTK chưa đầy đủ, tạm áp dụng phí vận chuyển mặc định.");
    }

    UriComponentsBuilder uriBuilder = UriComponentsBuilder
      .fromHttpUrl(normalizeBaseUrl(properties.getBaseUrl()))
      .path("/services/shipment/fee")
      .queryParam("pick_province", properties.getPickProvince().trim())
      .queryParam("pick_district", properties.getPickDistrict().trim())
      .queryParam("province", command.shipCity().trim())
      .queryParam("district", command.shipDistrict().trim())
      .queryParam("weight", weightGrams)
      .queryParam("value", command.orderValue().stripTrailingZeros().toPlainString());

    addOptionalQuery(uriBuilder, "pick_ward", properties.getPickWard());
    addOptionalQuery(uriBuilder, "pick_address", properties.getPickAddress());
    addOptionalQuery(uriBuilder, "address", command.shipAddress());
    addOptionalQuery(uriBuilder, "ward", command.shipWard());

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(List.of(MediaType.APPLICATION_JSON));
    headers.set("Token", properties.getToken().trim());
    headers.set("X-Client-Source", properties.getClientSource().trim());

    try {
      ResponseEntity<GhtkFeeResponse> response = restTemplate.exchange(
        uriBuilder.encode(StandardCharsets.UTF_8).build().toUri(),
        HttpMethod.GET,
        new HttpEntity<>(headers),
        GhtkFeeResponse.class
      );

      GhtkFeeResponse body = response.getBody();
      if (body == null) {
        throw new ApiValidationException("GHTK không trả về dữ liệu tính phí vận chuyển.");
      }

      if (!body.success() || body.fee() == null || body.fee().fee() == null) {
        throw new ApiValidationException(resolveMessage(body.message(), "GHTK không thể tính phí vận chuyển cho địa chỉ này."));
      }

      boolean deliverySupported = !Boolean.FALSE.equals(body.fee().delivery());
      return new ShippingFeeQuoteDTO(
        supportedProvider(),
        body.fee().fee(),
        body.fee().insuranceFee() == null ? BigDecimal.ZERO : body.fee().insuranceFee(),
        deliverySupported,
        weightGrams,
        resolveMessage(
          body.message(),
          deliverySupported
            ? "Đã tính phí vận chuyển qua GHTK thành công."
            : "GHTK hien chua ho tro giao den khu vuc nay."
        )
      );
    } catch (HttpStatusCodeException ex) {
      throw new ApiValidationException("GHTK từ chối yêu cầu tính phí vận chuyển. Vui lòng kiểm tra lại cấu hình và địa chỉ giao hàng.");
    } catch (RestClientException ex) {
      throw new ApiValidationException("Không thể kết nối GHTK để tính phí vận chuyển lúc này.");
    }
  }

  private boolean hasValidConfiguration() {
    return !isBlank(properties.getBaseUrl())
      && !isBlank(properties.getToken())
      && !isBlank(properties.getClientSource())
      && !isBlank(properties.getPickProvince())
      && !isBlank(properties.getPickDistrict())
      && properties.getDefaultWeightPerItemGrams() > 0;
  }

  private ShippingFeeQuoteDTO buildFallbackQuote(QuoteShippingFeeCommand command, int weightGrams, String message) {
    BigDecimal fee = isHoChiMinhCity(command.shipCity()) ? properties.getFallbackHcmFee() : properties.getFallbackOtherFee();
    if (fee == null || fee.signum() <= 0) {
      throw new ApiValidationException("Phí vận chuyển fallback của GHTK không hợp lệ.");
    }

    return new ShippingFeeQuoteDTO(
      supportedProvider(),
      fee,
      BigDecimal.ZERO,
      true,
      weightGrams,
      message
    );
  }

  private void addOptionalQuery(UriComponentsBuilder uriBuilder, String key, String value) {
    if (!isBlank(value)) {
      uriBuilder.queryParam(key, value.trim());
    }
  }

  private String resolveMessage(String providerMessage, String fallbackMessage) {
    return isBlank(providerMessage) ? fallbackMessage : providerMessage.trim();
  }

  private String normalizeBaseUrl(String baseUrl) {
    return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private boolean isHoChiMinhCity(String city) {
    if (isBlank(city)) {
      return false;
    }

    String normalizedCity = Normalizer.normalize(city, Normalizer.Form.NFD)
      .replaceAll("\\p{M}", "")
      .replaceAll("[^a-zA-Z0-9]", "")
      .toLowerCase();

    return normalizedCity.contains("hochiminh")
      || normalizedCity.contains("thanhphohochiminh")
      || normalizedCity.contains("hcm")
      || normalizedCity.contains("saigon");
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record GhtkFeeResponse(
    boolean success,
    String message,
    GhtkFeeData fee
  ) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record GhtkFeeData(
    BigDecimal fee,
    @JsonProperty("insurance_fee") BigDecimal insuranceFee,
    Boolean delivery
  ) {}
}
