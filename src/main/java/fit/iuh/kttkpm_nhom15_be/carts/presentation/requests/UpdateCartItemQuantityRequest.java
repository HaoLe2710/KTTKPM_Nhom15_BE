package fit.iuh.kttkpm_nhom15_be.carts.presentation.requests;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCartItemQuantityRequest {

  @Min(value = 0, message = "So luong san pham trong gio hang khong duoc am")
  private int quantity;
}
