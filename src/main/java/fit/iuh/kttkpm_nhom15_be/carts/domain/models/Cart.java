package fit.iuh.kttkpm_nhom15_be.carts.domain.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    private String id;
    private String userId;
    private CartStatus status;
    private List<CartItem> items;
}