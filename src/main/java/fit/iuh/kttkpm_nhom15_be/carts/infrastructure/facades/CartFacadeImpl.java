package fit.iuh.kttkpm_nhom15_be.carts.infrastructure.facades;

import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartDTO;
import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartItemDTO;
import fit.iuh.kttkpm_nhom15_be.carts.application.interfaces.CartFacade;
import fit.iuh.kttkpm_nhom15_be.carts.domain.models.Cart;
import fit.iuh.kttkpm_nhom15_be.carts.domain.models.CartStatus;
import fit.iuh.kttkpm_nhom15_be.carts.domain.repositories.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of CartFacade interacting with the real Cart Database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CartFacadeImpl implements CartFacade {

  private final CartRepository cartRepository;

  @Override
  @Transactional(readOnly = true)
  public CartDTO getActiveCart(String userId) {
    Cart cart = cartRepository.findActiveCart(userId)
      .orElseThrow(() -> new IllegalStateException("Giỏ hàng của người dùng '" + userId + "' đang trống hoặc không tồn tại."));

    if (cart.getItems() == null || cart.getItems().isEmpty()) {
      throw new IllegalStateException("Giỏ hàng của người dùng '" + userId + "' đang trống");
    }

    List<CartItemDTO> itemDTOs = cart.getItems().stream()
      .map(item -> CartItemDTO.builder()
        .variantId(item.getVariantId())
        .quantity(item.getQuantity())
        .price(item.getUnitPrice())
        .build())
      .toList();

    return CartDTO.builder()
      .cartId(cart.getId())
      .userId(cart.getUserId())
      .items(itemDTOs)
      .build();
  }

  @Override
  @Transactional
  public void clearCart(String userId) {
    cartRepository.findActiveCart(userId).ifPresent(cart -> {
      log.info("Updating cart {} status from ACTIVE to CHECKED_OUT for user {}", cart.getId(), userId);
      cart.setStatus(CartStatus.CHECKED_OUT);
      cartRepository.save(cart);
    });
  }
}
