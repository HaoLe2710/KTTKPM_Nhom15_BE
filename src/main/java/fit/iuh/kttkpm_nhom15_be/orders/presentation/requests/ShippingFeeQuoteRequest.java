package fit.iuh.kttkpm_nhom15_be.orders.presentation.requests;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingFeeQuoteRequest {

  @NotNull(message = "shippingProvider không được để trống")
  private ShippingProvider shippingProvider;

  private String shipAddress;

  @NotBlank(message = "shipCity không được để trống")
  private String shipCity;

  @NotBlank(message = "shipDistrict không được để trống")
  private String shipDistrict;

  private String shipWard;

  @NotNull(message = "orderValue không được để trống")
  @Positive(message = "orderValue phai lon hon 0")
  private BigDecimal orderValue;

  @NotNull(message = "itemQuantity không được để trống")
  @Positive(message = "itemQuantity phai lon hon 0")
  private Integer itemQuantity;
}
