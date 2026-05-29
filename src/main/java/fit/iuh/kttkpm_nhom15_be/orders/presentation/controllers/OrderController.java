package fit.iuh.kttkpm_nhom15_be.orders.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.orders.application.commands.CancelOrderCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.commands.PlaceOrderCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.commands.QuoteShippingFeeCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.OrderDetailDTO;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.OrderHistoryItemDTO;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.ShippingFeeQuoteDTO;
import fit.iuh.kttkpm_nhom15_be.orders.application.results.CancelOrderResult;
import fit.iuh.kttkpm_nhom15_be.orders.application.results.PlaceOrderResult;
import fit.iuh.kttkpm_nhom15_be.orders.application.usecases.CancelOrderUseCase;
import fit.iuh.kttkpm_nhom15_be.orders.application.usecases.GetMyOrderDetailUseCase;
import fit.iuh.kttkpm_nhom15_be.orders.application.usecases.GetMyOrderHistoryUseCase;
import fit.iuh.kttkpm_nhom15_be.orders.application.usecases.PlaceOrderUseCase;
import fit.iuh.kttkpm_nhom15_be.orders.application.usecases.QuoteShippingFeeUseCase;
import fit.iuh.kttkpm_nhom15_be.orders.presentation.requests.CancelOrderRequest;
import fit.iuh.kttkpm_nhom15_be.orders.presentation.requests.PlaceOrderRequest;
import fit.iuh.kttkpm_nhom15_be.orders.presentation.requests.ShippingFeeQuoteRequest;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.ShopperAccessGuard;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.advice.ApiSuccessMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

  private final PlaceOrderUseCase placeOrderUseCase;
  private final CancelOrderUseCase cancelOrderUseCase;
  private final QuoteShippingFeeUseCase quoteShippingFeeUseCase;
  private final GetMyOrderDetailUseCase getMyOrderDetailUseCase;
  private final GetMyOrderHistoryUseCase getMyOrderHistoryUseCase;
  private final ShopperAccessGuard shopperAccessGuard;

  @GetMapping("/history")
  @ApiSuccessMessage("Lấy lịch sử đơn hàng thành công")
  public ResponseEntity<List<OrderHistoryItemDTO>> getMyOrderHistory(Principal principal) {
    return ResponseEntity.ok(getMyOrderHistoryUseCase.execute(principal.getName()));
  }

  @GetMapping("/{orderId}")
  @ApiSuccessMessage("Lấy chi tiết đơn hàng thành công")
  public ResponseEntity<OrderDetailDTO> getMyOrderDetail(@PathVariable String orderId, Principal principal) {
    return ResponseEntity.ok(getMyOrderDetailUseCase.execute(principal.getName(), orderId));
  }

  @PostMapping("/shipping-fee/quote")
  @ApiSuccessMessage("Tính phí vận chuyển thành công")
  public ResponseEntity<ShippingFeeQuoteDTO> quoteShippingFee(@Valid @RequestBody ShippingFeeQuoteRequest request) {
    QuoteShippingFeeCommand command = new QuoteShippingFeeCommand(
      request.getShippingProvider(),
      request.getShipAddress(),
      request.getShipCity(),
      request.getShipDistrict(),
      request.getShipWard(),
      request.getOrderValue(),
      request.getItemQuantity()
    );
    return ResponseEntity.ok(quoteShippingFeeUseCase.execute(command));
  }

  @PostMapping
  @ApiSuccessMessage("Đặt hàng thành công")
  public ResponseEntity<PlaceOrderResult> placeOrder(@Valid @RequestBody PlaceOrderRequest request,
                                                     HttpServletRequest httpServletRequest) {
    String resolvedUserId = shopperAccessGuard.resolveAllowedUserId(request.getUserId());
    PlaceOrderCommand command = PlaceOrderCommand.builder()
      .userId(resolvedUserId)
      .promotionCode(request.getPromotionCode())
      .clientIp(resolveClientIp(httpServletRequest))
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

  @PostMapping("/{orderId}/cancel")
  @ApiSuccessMessage("Hủy đơn hàng thành công")
  public ResponseEntity<CancelOrderResult> cancelOrder(
    @PathVariable String orderId,
    @Valid @RequestBody CancelOrderRequest request
  ) {
    CancelOrderCommand command = new CancelOrderCommand(orderId, request.getReason());
    CancelOrderResult result = cancelOrderUseCase.execute(command);
    return ResponseEntity.ok(result);
  }

  private String resolveClientIp(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
