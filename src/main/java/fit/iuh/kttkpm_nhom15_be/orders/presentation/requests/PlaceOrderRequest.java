package fit.iuh.kttkpm_nhom15_be.orders.presentation.requests;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingMode;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingProvider;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {

  @NotBlank(message = "userId không được để trống")
  private String userId;

  private String promotionCode;

  // Thông tin nhận hàng
  @NotBlank(message = "Tên người nhận không được để trống")
  private String shipFullName;

  @NotBlank(message = "Số điện thoại không được để trống")
  private String shipPhone;

  @NotBlank(message = "Email nguoi nhan khong duoc de trong")
  @Email(message = "Email nguoi nhan khong hop le")
  private String shipEmail;

  @NotBlank(message = "Địa chỉ không được để trống")
  private String shipAddress;

  @NotBlank(message = "Tỉnh/Thành phố không được để trống")
  private String shipCity;

  @NotBlank(message = "Quận/Huyện không được để trống")
  private String shipDistrict;

  @NotBlank(message = "Phường/Xã không được để trống")
  private String shipWard;

  // Vận chuyển & Thanh toán
  @NotNull(message = "Phương thức vận chuyển không được để trống")
  private ShippingMode shippingMode;

  private ShippingProvider shippingProvider;

  @NotNull(message = "Phí vận chuyển không được để trống")
  @Positive(message = "Phí vận chuyển phải lớn hơn 0")
  private BigDecimal shippingFee;

  @NotNull(message = "Phương thức thanh toán không được để trống")
  private PaymentMethod paymentMethod;
}
