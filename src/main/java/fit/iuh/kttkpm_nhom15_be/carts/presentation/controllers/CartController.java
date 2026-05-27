package fit.iuh.kttkpm_nhom15_be.carts.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.carts.application.commands.AddToCartCommand;
import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.carts.application.interfaces.CartFacade;
import fit.iuh.kttkpm_nhom15_be.carts.application.usecases.AddToCartUseCase;
import fit.iuh.kttkpm_nhom15_be.carts.presentation.requests.AddToCartRequest;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.ShopperAccessGuard;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.advice.ApiSuccessMessage;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
  private final CartFacade cartFacade;
  private final ShopperAccessGuard shopperAccessGuard;

  @PostMapping("/items")
  @ApiSuccessMessage("Cap nhat gio hang thanh cong")
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

  @DeleteMapping("/active")
  public ResponseEntity<MessageResponse> archiveActiveCart(@RequestHeader("X-User-Id") String userId) {
    cartFacade.clearCart(shopperAccessGuard.resolveAllowedUserId(userId));
    return ResponseEntity.ok(new MessageResponse("Gio hang hien tai da duoc lam moi thanh cong"));
  }
}
