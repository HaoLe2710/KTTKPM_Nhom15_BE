package fit.iuh.kttkpm_nhom15_be.carts.domain.models;

import lombok.*;

import java.math.BigDecimal;
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

    /**
     * Thêm sản phẩm vào giỏ hàng. Nếu đã tồn tại thì cộng dồn số lượng.
     */
    public void addItem(String variantId, int quantity, BigDecimal unitPrice) {
        if (this.items == null) {
            this.items = new java.util.ArrayList<>();
        }
        
        for (CartItem item : this.items) {
            if (item.getVariantId().equals(variantId)) {
                item.setQuantity(item.getQuantity() + quantity);
                item.setUnitPrice(unitPrice); // Cập nhật giá mới nhất
                return;
            }
        }
        
        this.items.add(CartItem.builder()
            .variantId(variantId)
            .quantity(quantity)
            .unitPrice(unitPrice)
            .build());
    }

    /**
     * Tính tổng số lượng sản phẩm trong giỏ.
     */
    public int calculateTotalItems() {
        if (this.items == null) return 0;
        return this.items.stream()
            .mapToInt(CartItem::getQuantity)
            .sum();
    }

    /**
     * Tính tổng giá trị giỏ hàng.
     */
    public BigDecimal calculateSubtotal() {
        if (this.items == null) return BigDecimal.ZERO;
        return this.items.stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}