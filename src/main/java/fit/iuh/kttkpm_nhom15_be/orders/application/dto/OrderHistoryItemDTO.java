package fit.iuh.kttkpm_nhom15_be.orders.application.dto;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderHistoryItemDTO(
  String orderId,
  String orderNo,
  PaymentMethod paymentMethod,
  PaymentStatus paymentStatus,
  OrderStatus status,
  String receiverName,
  BigDecimal totalAmount,
  LocalDateTime createdAt
) {}
