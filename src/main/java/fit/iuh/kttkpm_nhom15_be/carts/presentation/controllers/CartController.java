package fit.iuh.kttkpm_nhom15_be.carts.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.carts.application.commands.AddToCartCommand;
import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.carts.application.usecases.AddToCartUseCase;
import fit.iuh.kttkpm_nhom15_be.carts.presentation.requests.AddToCartRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.exceptions.ProductUnavailableException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {

  private final AddToCartUseCase addToCartUseCase;

  /**
   * Khách hàng / Guest gọi API này để thêm sản phẩm vào giỏ.
   * Yêu cầu truyền Header X-User-Id hoặc xác thực JWT (ở đây ta giả lập qua Request Header).
   */
  @PostMapping("/items")
  public ResponseEntity<CartSummaryDTO> addToCart(
    @RequestHeader("X-User-Id") String userId,
    @Valid @RequestBody AddToCartRequest request
  ) {
    AddToCartCommand command = new AddToCartCommand(
      userId,
      request.getVariantId(),
      request.getQuantity()
    );
    CartSummaryDTO summary = addToCartUseCase.execute(command);
    return ResponseEntity.ok(summary);
  }

  // --- Exception Handlers ---
  
  @ExceptionHandler(ProductUnavailableException.class)
  public ResponseEntity<Map<String, String>> handleProductUnavailable(ProductUnavailableException ex) {
    // Trả về HTTP 400 Bad Request theo thiết kế sequence diagram
    return ResponseEntity.badRequest().body(
      Map.of("error", ex.getMessage())
    );
  }
}
