package fit.iuh.kttkpm_nhom15_be.carts.application.usecases;

import fit.iuh.kttkpm_nhom15_be.carts.application.commands.UpdateCartItemQuantityCommand;
import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.carts.domain.models.Cart;
import fit.iuh.kttkpm_nhom15_be.carts.domain.models.CartStatus;
import fit.iuh.kttkpm_nhom15_be.carts.domain.repositories.CartRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.VariantInfoDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.interfaces.CatalogFacade;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateCartItemQuantityUseCase {

  private final CartRepository cartRepository;
  private final CatalogFacade catalogFacade;

  @Transactional
  public CartSummaryDTO execute(UpdateCartItemQuantityCommand command) {
    if (command.quantity() < 0) {
      throw new ApiValidationException("So luong san pham trong gio hang khong duoc am.");
    }

    Cart cart = cartRepository.findActiveCart(command.userId())
      .orElseGet(() -> Cart.builder()
        .userId(command.userId())
        .status(CartStatus.ACTIVE)
        .items(new java.util.ArrayList<>())
        .build());

    if (command.quantity() == 0) {
      cart.removeItem(command.variantId());
    } else {
      VariantInfoDTO variantInfo = catalogFacade.checkAvailabilityAndPrice(command.variantId(), command.quantity());
      cart.setItemQuantity(command.variantId(), command.quantity(), variantInfo.price());
    }

    Cart savedCart = cartRepository.save(cart);
    return new CartSummaryDTO(savedCart.calculateTotalItems(), savedCart.calculateSubtotal());
  }
}
