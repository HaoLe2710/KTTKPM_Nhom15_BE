package fit.iuh.kttkpm_nhom15_be.orders.presentation.controllers.admin;

import fit.iuh.kttkpm_nhom15_be.orders.application.dto.admin.OrderAdminDtos.AdminOrderQuickCancelRequest;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.admin.OrderAdminDtos.AdminOrderStatusUpdateRequest;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.admin.OrderAdminDtos.OrderAdminDetailResponse;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.admin.OrderAdminDtos.OrderAdminSummaryRow;
import fit.iuh.kttkpm_nhom15_be.orders.application.services.OrderAdminService;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest.SortDirection;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.MessageResponse;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.support.AdminPageRequestFactory;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','STAFF')")
public class AdminOrderController {

  private static final Set<String> ORDER_SORT_FIELDS = Set.of("createdAt", "orderNo", "status", "paymentStatus", "totalAmount");

  private final OrderAdminService orderAdminService;
  private final AdminPageRequestFactory adminPageRequestFactory;

  @GetMapping
  public ResponseEntity<Page<OrderAdminSummaryRow>> getOrders(@RequestParam(required = false) String q,
                                                              @RequestParam(required = false) OrderStatus status,
                                                              @RequestParam(required = false) PaymentStatus paymentStatus,
                                                              @RequestParam(required = false) Integer page,
                                                              @RequestParam(required = false) Integer size,
                                                              @RequestParam(required = false) String sort) {
    return ResponseEntity.ok(orderAdminService.findOrders(
      q,
      status,
      paymentStatus,
      adminPageRequestFactory.create(page, size, sort, "createdAt", SortDirection.DESC, ORDER_SORT_FIELDS)
    ));
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<OrderAdminDetailResponse> getOrderDetail(@PathVariable String orderId) {
    return ResponseEntity.ok(orderAdminService.getOrderDetail(orderId));
  }

  @PatchMapping("/{orderId}/status")
  public ResponseEntity<OrderAdminDetailResponse> updateStatus(@PathVariable String orderId,
                                                               @Valid @RequestBody AdminOrderStatusUpdateRequest request) {
    return ResponseEntity.ok(orderAdminService.updateStatus(orderId, request));
  }

  @PatchMapping("/{orderId}/confirm")
  public ResponseEntity<MessageResponse> confirm(@PathVariable String orderId) {
    orderAdminService.updateStatus(orderId, new AdminOrderStatusUpdateRequest(OrderStatus.CONFIRMED, null));
    return ResponseEntity.ok(new MessageResponse("Đơn hàng đã được xác nhận."));
  }

  @PatchMapping("/{orderId}/ship")
  public ResponseEntity<MessageResponse> ship(@PathVariable String orderId) {
    orderAdminService.updateStatus(orderId, new AdminOrderStatusUpdateRequest(OrderStatus.SHIPPING, null));
    return ResponseEntity.ok(new MessageResponse("Đơn hàng đã chuyển sang trạng thái giao hàng."));
  }

  @PatchMapping("/{orderId}/complete")
  public ResponseEntity<MessageResponse> complete(@PathVariable String orderId) {
    orderAdminService.updateStatus(orderId, new AdminOrderStatusUpdateRequest(OrderStatus.COMPLETED, null));
    return ResponseEntity.ok(new MessageResponse("Đơn hàng đã hoàn thành."));
  }

  @PatchMapping("/{orderId}/cancel")
  public ResponseEntity<MessageResponse> cancel(@PathVariable String orderId,
                                                @Valid @RequestBody AdminOrderQuickCancelRequest request) {
    orderAdminService.updateStatus(orderId, new AdminOrderStatusUpdateRequest(OrderStatus.CANCELLED, request.reason()));
    return ResponseEntity.ok(new MessageResponse("Đơn hàng đã được hủy."));
  }
}

