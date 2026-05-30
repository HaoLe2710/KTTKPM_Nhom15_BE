package fit.iuh.kttkpm_nhom15_be.orders.presentation.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderRequest {

  @NotBlank(message = "userId không được để trống")
  private String userId;

  @NotBlank(message = "Lý do hủy đơn không được để trống")
  private String reason;
}
