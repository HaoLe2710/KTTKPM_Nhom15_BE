package fit.iuh.kttkpm_nhom15_be.carts.domain.models;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private String id;
    private String variantId;
    private int quantity;
    private BigDecimal unitPrice;
}