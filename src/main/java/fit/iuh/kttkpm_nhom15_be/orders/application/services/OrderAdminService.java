package fit.iuh.kttkpm_nhom15_be.orders.application.services;

import fit.iuh.kttkpm_nhom15_be.orders.application.dto.admin.OrderAdminDtos.AdminOrderStatusUpdateRequest;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.admin.OrderAdminDtos.OrderAdminDetailResponse;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.admin.OrderAdminDtos.OrderAdminSummaryRow;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderAdminRepository;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiNotFoundException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderAdminService {

  private final OrderAdminRepository orderAdminRepository;
  private final OrderRepository orderRepository;

  public Page<OrderAdminSummaryRow> findOrders(String query,
                                               OrderStatus status,
                                               PaymentStatus paymentStatus,
                                               AdminPageRequest pageRequest) {
    return orderAdminRepository.findOrders(query, status, paymentStatus, pageRequest);
  }

  public OrderAdminDetailResponse getOrderDetail(String orderId) {
    return orderAdminRepository.findOrderDetail(orderId)
      .orElseThrow(() -> new ApiNotFoundException("Order not found"));
  }

  public OrderAdminDetailResponse updateStatus(String orderId, AdminOrderStatusUpdateRequest request) {
    Order order = orderRepository.findById(orderId)
      .orElseThrow(() -> new ApiNotFoundException("Order not found"));

    applyStatus(order, request.status(), request.reason());
    orderRepository.save(order);
    return getOrderDetail(orderId);
  }

  private void applyStatus(Order order, OrderStatus target, String reason) {
    if (target == null) {
      throw new ApiValidationException("status không hợp lệ.");
    }
    switch (target) {
      case CONFIRMED -> order.confirmOrder();
      case SHIPPING -> order.shipOrder();
      case COMPLETED -> order.completeOrder();
      case CANCELLED -> {
        if (reason == null || reason.isBlank()) {
          throw new ApiValidationException("reason là bắt buộc khi hủy đơn.");
        }
        order.cancelOrder(reason.trim());
      }
      case CREATED -> throw new ApiValidationException("Không thể chuyển đơn hàng về CREATED.");
    }
  }
}

