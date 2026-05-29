package fit.iuh.kttkpm_nhom15_be.carts.application.usecases;

import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.carts.domain.models.Cart;
import fit.iuh.kttkpm_nhom15_be.carts.domain.repositories.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemoveCartItemUseCase {

  private final CartRepository cartRepository;

  @Transactional
  public CartSummaryDTO execute(String userId, String variantId) {
    Cart cart = cartRepository.findActiveCart(userId).orElse(null);
    if (cart == null) {
      return new CartSummaryDTO(0, java.math.BigDecimal.ZERO);
    }

    cart.removeItem(variantId);
    Cart savedCart = cartRepository.save(cart);
    return new CartSummaryDTO(savedCart.calculateTotalItems(), savedCart.calculateSubtotal());
  }
}
