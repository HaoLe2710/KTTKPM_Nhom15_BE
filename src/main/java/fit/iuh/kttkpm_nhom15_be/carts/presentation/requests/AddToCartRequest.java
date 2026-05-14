package fit.iuh.kttkpm_nhom15_be.carts.presentation.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddToCartRequest {

  @NotBlank(message = "Variant ID không được để trống")
  private String variantId;

  @Positive(message = "Số lượng phải lớn hơn 0")
  private int quantity;
}
