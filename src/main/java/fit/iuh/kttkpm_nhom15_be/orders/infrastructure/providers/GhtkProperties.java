package fit.iuh.kttkpm_nhom15_be.orders.infrastructure.providers;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ghtk")
public class GhtkProperties {
  private String baseUrl = "https://services.giaohangtietkiem.vn";
  private String token;
  private String clientSource;
  private String pickProvince;
  private String pickDistrict;
  private String pickWard;
  private String pickAddress;
  private int defaultWeightPerItemGrams = 500;
  private BigDecimal fallbackHcmFee = BigDecimal.valueOf(15000);
  private BigDecimal fallbackOtherFee = BigDecimal.valueOf(30000);
}
