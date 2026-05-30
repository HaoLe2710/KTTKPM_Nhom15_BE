package fit.iuh.kttkpm_nhom15_be.orders.application.dto.admin;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class OrderAdminDtos {

  private OrderAdminDtos() {
  }

  public record OrderAdminSummaryRow(
    String id,
    String orderNo,
    String userId,
    String customerName,
    String customerEmail,
    OrderStatus status,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    BigDecimal totalAmount,
    int itemCount,
    LocalDateTime createdAt
  ) {
  }

  public record OrderAdminItemRow(
    String id,
    String productId,
    String variantId,
    String sku,
    String name,
    String imageUrl,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal
  ) {
  }

  public record OrderAdminDetailResponse(
    String id,
    String orderNo,
    String userId,
    String customerName,
    String customerEmail,
    String shipFullName,
    String shipPhone,
    String shipAddress,
    String shipCity,
    String shipDistrict,
    String shipWard,
    OrderStatus status,
    PaymentMethod paymentMethod,
    PaymentStatus paymentStatus,
    BigDecimal subtotalAmount,
    BigDecimal discountAmount,
    BigDecimal shippingFee,
    BigDecimal totalAmount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<OrderAdminItemRow> items
  ) {
  }

  public record AdminOrderStatusUpdateRequest(
    @NotNull(message = "status không được để trống")
    OrderStatus status,
    String reason
  ) {
  }

  public record AdminOrderQuickCancelRequest(
    @NotBlank(message = "reason không được để trống")
    String reason
  ) {
  }
}

