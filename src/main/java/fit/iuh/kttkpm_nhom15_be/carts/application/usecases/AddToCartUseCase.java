package fit.iuh.kttkpm_nhom15_be.carts.application.usecases;

import fit.iuh.kttkpm_nhom15_be.carts.application.commands.AddToCartCommand;
import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.carts.domain.models.Cart;
import fit.iuh.kttkpm_nhom15_be.carts.domain.models.CartStatus;
import fit.iuh.kttkpm_nhom15_be.carts.domain.repositories.CartRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.VariantInfoDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.interfaces.CatalogFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddToCartUseCase {

  private final CartRepository cartRepository;
  private final CatalogFacade catalogFacade;

  @Transactional
  public CartSummaryDTO execute(AddToCartCommand command) {
    // 1. Giao tiếp đồng bộ để check tồn kho & lấy giá 
    //    (ném ProductUnavailableException nếu không thể thêm)
    VariantInfoDTO variantInfo = catalogFacade.checkAvailabilityAndPrice(command.variantId(), command.quantity());

    // 2. Tìm Active Cart của user
    Cart cart = cartRepository.findActiveCart(command.userId())
      .orElseGet(() -> {
        // 3. Nếu chưa có, khởi tạo giỏ hàng mới
        Cart newCart = Cart.builder()
          .userId(command.userId())
          .status(CartStatus.ACTIVE)
          .items(new java.util.ArrayList<>())
          .build();
        return cartRepository.save(newCart);
      });

    // 4. Ủy quyền cho Domain Model tự cập nhật CartItem (check duplicate / add mới)
    cart.addItem(command.variantId(), command.quantity(), variantInfo.price());

    // 5. Lưu xuống DB
    Cart savedCart = cartRepository.save(cart);

    // 6. Trả về CartSummary theo đúng thiết kế Sequence Diagram
    return new CartSummaryDTO(
      savedCart.calculateTotalItems(),
      savedCart.calculateSubtotal()
    );
  }
}
