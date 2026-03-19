package fit.iuh.kttkpm_nhom15_be.carts.infrastructure.facades;

import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartDTO;
import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartItemDTO;
import fit.iuh.kttkpm_nhom15_be.carts.application.interfaces.CartFacade;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Stub implementation of CartFacade.
 * TODO: Replace with real JPA-backed implementation that queries the carts module DB.
 */
@Component
public class CartFacadeImpl implements CartFacade {

  @Override
  public CartDTO getActiveCart(String userId) {
    // Stub: trả về giỏ hàng mẫu với 1 item để test flow
    // TODO: truy vấn bảng carts + cart_items theo userId và status = 'ACTIVE'
    CartItemDTO item = CartItemDTO.builder()
      .variantId("variant-demo-001")
      .quantity(2)
      .price(new BigDecimal("150000"))
      .build();

    CartDTO cart = CartDTO.builder()
      .cartId("cart-demo-001")
      .userId(userId)
      .items(List.of(item))
      .build();

    if (cart.getItems() == null || cart.getItems().isEmpty()) {
      throw new IllegalStateException("Giỏ hàng của người dùng '" + userId + "' đang trống");
    }

    return cart;
  }

  @Override
  public void clearCart(String userId) {
    // Stub: log xóa giỏ hàng
    // TODO: cập nhật status bản ghi cart thành 'CLEARED' hoặc xóa cart_items
    System.out.println("[CartFacadeImpl] Đã xóa giỏ hàng của user: " + userId);
  }
}
