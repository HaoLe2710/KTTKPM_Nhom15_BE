package fit.iuh.kttkpm_nhom15_be.orders.application.dto;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailDTO(
  String orderId,
  String orderNo,
  OrderStatus status,
  PaymentMethod paymentMethod,
  PaymentStatus paymentStatus,
  BigDecimal subtotalAmount,
  BigDecimal discountAmount,
  BigDecimal shippingFee,
  BigDecimal totalAmount,
  String shipFullName,
  String shipPhone,
  String shipAddress,
  String shipCity,
  String shipDistrict,
  String shipWard,
  LocalDateTime createdAt,
  List<OrderDetailItemDTO> items
) {}
