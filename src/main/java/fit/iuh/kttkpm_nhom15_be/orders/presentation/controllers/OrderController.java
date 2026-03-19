package fit.iuh.kttkpm_nhom15_be.orders.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.orders.application.commands.PlaceOrderCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.results.PlaceOrderResult;
import fit.iuh.kttkpm_nhom15_be.orders.application.usecases.PlaceOrderUseCase;
import fit.iuh.kttkpm_nhom15_be.orders.presentation.requests.PlaceOrderRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final PlaceOrderUseCase placeOrderUseCase;

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
}
