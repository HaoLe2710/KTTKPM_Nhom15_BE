package fit.iuh.kttkpm_nhom15_be.carts.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.carts.application.commands.AddToCartCommand;
import fit.iuh.kttkpm_nhom15_be.carts.application.commands.UpdateCartItemQuantityCommand;
import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.carts.application.interfaces.CartFacade;
import fit.iuh.kttkpm_nhom15_be.carts.application.usecases.AddToCartUseCase;
import fit.iuh.kttkpm_nhom15_be.carts.application.usecases.RemoveCartItemUseCase;
import fit.iuh.kttkpm_nhom15_be.carts.application.usecases.UpdateCartItemQuantityUseCase;
import fit.iuh.kttkpm_nhom15_be.carts.presentation.requests.AddToCartRequest;
import fit.iuh.kttkpm_nhom15_be.carts.presentation.requests.UpdateCartItemQuantityRequest;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.ShopperAccessGuard;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.advice.ApiSuccessMessage;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

  private final AddToCartUseCase addToCartUseCase;
  private final UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase;
  private final RemoveCartItemUseCase removeCartItemUseCase;
  private final CartFacade cartFacade;
  private final ShopperAccessGuard shopperAccessGuard;

  @PostMapping("/items")
  @ApiSuccessMessage("Cập nhật giỏ hàng thành công")
  public ResponseEntity<CartSummaryDTO> addToCart(
    @RequestHeader("X-User-Id") String userId,
    @Valid @RequestBody AddToCartRequest request
  ) {
    String resolvedUserId = shopperAccessGuard.resolveAllowedUserId(userId);
    AddToCartCommand command = new AddToCartCommand(
      resolvedUserId,
      request.getVariantId(),
      request.getQuantity()
    );
    CartSummaryDTO summary = addToCartUseCase.execute(command);
    return ResponseEntity.ok(summary);
  }

  @PatchMapping("/items/{variantId}")
  @ApiSuccessMessage("Cáº­p nháº­t giá» hÃ ng thÃ nh cÃ´ng")
  public ResponseEntity<CartSummaryDTO> updateCartItemQuantity(
    @RequestHeader("X-User-Id") String userId,
    @PathVariable String variantId,
    @Valid @RequestBody UpdateCartItemQuantityRequest request
  ) {
    String resolvedUserId = shopperAccessGuard.resolveAllowedUserId(userId);
    CartSummaryDTO summary = updateCartItemQuantityUseCase.execute(new UpdateCartItemQuantityCommand(
      resolvedUserId,
      variantId,
      request.getQuantity()
    ));
    return ResponseEntity.ok(summary);
  }

  @DeleteMapping("/items/{variantId}")
  @ApiSuccessMessage("XÃ³a sáº£n pháº©m khá»i giá» hÃ ng thÃ nh cÃ´ng")
  public ResponseEntity<CartSummaryDTO> removeCartItem(
    @RequestHeader("X-User-Id") String userId,
    @PathVariable String variantId
  ) {
    String resolvedUserId = shopperAccessGuard.resolveAllowedUserId(userId);
    return ResponseEntity.ok(removeCartItemUseCase.execute(resolvedUserId, variantId));
  }

  @DeleteMapping("/active")
  public ResponseEntity<MessageResponse> archiveActiveCart(@RequestHeader("X-User-Id") String userId) {
    cartFacade.clearCart(shopperAccessGuard.resolveAllowedUserId(userId));
    return ResponseEntity.ok(new MessageResponse("Giỏ hàng hiện tại đã được làm mới thành công"));
  }
}
