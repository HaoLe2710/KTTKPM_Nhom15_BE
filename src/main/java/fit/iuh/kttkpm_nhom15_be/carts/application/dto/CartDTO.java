package fit.iuh.kttkpm_nhom15_be.carts.application.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private String cartId;
    private String userId;
    private List<CartItemDTO> items;
}