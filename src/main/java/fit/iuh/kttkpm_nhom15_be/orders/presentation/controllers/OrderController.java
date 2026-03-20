package fit.iuh.kttkpm_nhom15_be.orders.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.orders.application.commands.CancelOrderCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.commands.PlaceOrderCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.results.CancelOrderResult;
import fit.iuh.kttkpm_nhom15_be.orders.application.results.PlaceOrderResult;
import fit.iuh.kttkpm_nhom15_be.orders.application.usecases.CancelOrderUseCase;
import fit.iuh.kttkpm_nhom15_be.orders.application.usecases.PlaceOrderUseCase;
import fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions.InvalidOrderStateTransitionException;
import fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions.OrderNotFoundException;
import fit.iuh.kttkpm_nhom15_be.orders.presentation.requests.CancelOrderRequest;
import fit.iuh.kttkpm_nhom15_be.orders.presentation.requests.PlaceOrderRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final PlaceOrderUseCase placeOrderUseCase;
  private final CancelOrderUseCase cancelOrderUseCase;

  /**
   * POST /api/v1/orders
   * Đặt hàng: lấy giỏ hàng, kiểm tra tồn kho, tạo Order với State Pattern, publish event.
   */
  @PostMapping
  public ResponseEntity<PlaceOrderResult> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
    PlaceOrderCommand command = PlaceOrderCommand.builder()
      .userId(request.getUserId())
      .shipFullName(request.getShipFullName())
      .shipPhone(request.getShipPhone())
      .shipAddress(request.getShipAddress())
      .shipCity(request.getShipCity())
      .shipDistrict(request.getShipDistrict())
      .shipWard(request.getShipWard())
      .shippingMode(request.getShippingMode())
      .shippingProvider(request.getShippingProvider())
      .shippingFee(request.getShippingFee())
      .paymentMethod(request.getPaymentMethod())
      .build();

    PlaceOrderResult result = placeOrderUseCase.execute(command);
    return ResponseEntity.status(HttpStatus.CREATED).body(result);
  }

  /**
   * POST /api/v1/orders/{orderId}/cancel
   * Hủy đơn hàng: áp dụng State Pattern, hoàn tồn kho, publish OrderCancelledEvent.
   */
  @PostMapping("/{orderId}/cancel")
  public ResponseEntity<CancelOrderResult> cancelOrder(
    @PathVariable String orderId,
    @Valid @RequestBody CancelOrderRequest request
  ) {
    CancelOrderCommand command = new CancelOrderCommand(orderId, request.getReason());
    CancelOrderResult result = cancelOrderUseCase.execute(command);
    return ResponseEntity.ok(result);
  }

  // --- Exception Handlers ---

  @ExceptionHandler(InvalidOrderStateTransitionException.class)
  public ResponseEntity<Map<String, String>> handleInvalidStateTransition(InvalidOrderStateTransitionException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
      Map.of("error", ex.getMessage())
    );
  }

  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleOrderNotFound(OrderNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
      Map.of("error", ex.getMessage())
    );
  }
}

