package fit.iuh.kttkpm_nhom15_be.carts.application.interfaces;

import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartDTO;

public interface CartFacade {
  // Lấy thông tin giỏ hàng hiện tại của user
  CartDTO getActiveCart(String userId);

  // Xóa cứng giỏ hàng sau khi đặt hàng thành công
  void clearCart(String userId);
}