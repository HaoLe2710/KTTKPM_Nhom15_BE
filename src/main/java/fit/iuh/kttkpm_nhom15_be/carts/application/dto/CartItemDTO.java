package fit.iuh.kttkpm_nhom15_be.carts.application.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private String variantId;
    private int quantity;
    private BigDecimal price;
}