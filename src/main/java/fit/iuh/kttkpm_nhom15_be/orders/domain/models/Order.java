package fit.iuh.kttkpm_nhom15_be.orders.domain.models;

import fit.iuh.kttkpm_nhom15_be.orders.domain.states.*;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
  private String id;
  private String orderNo;
  private String userId;
  private BigDecimal subtotalAmount;
  private BigDecimal discountAmount;
  private BigDecimal shippingFee;
  private BigDecimal totalAmount;
  private String promotionId;
  private String promotionCode;
  private OrderStatus status;
  private PaymentMethod paymentMethod;
  private PaymentStatus paymentStatus;
  private boolean stockDeducted;

  private String shipFullName;
  private String shipPhone;
  private String shipAddress;
  private String shipCity;
  private String shipDistrict;
  private String shipWard;

  private ShippingMode shippingMode;
  private ShippingProvider shippingProvider;
  private Map<String, Object> shippingMeta;

  private String cancelReason;

  private List<OrderItem> items;

  private transient OrderState state;

  /**
   * Factory khởi tạo đúng State tương ứng với status hiện tại của đơn hàng.
   * Phải gọi sau khi build() hoặc sau khi load từ DB.
   */
  public void initBehavior() {
    if (this.status == null) {
      this.status = OrderStatus.CREATED;
    }

    this.state = switch (this.status) {
      case CREATED -> new CreatedState();
      case CONFIRMED -> new ConfirmedState();
      case SHIPPING -> new ShippingState();
      case COMPLETED -> new CompletedState();
      case CANCELLED -> new CancelledState();
    };
  }

  // --- Ủy quyền nghiệp vụ sang State ---

  public void confirmOrder() {
    if (this.state == null) initBehavior();
    this.state.confirm(this);
  }

  public void cancelOrder(String reason) {
    if (this.state == null) initBehavior();
    this.cancelReason = reason;
    this.state.cancel(this, reason);
  }

  public void shipOrder() {
    if (this.state == null) initBehavior();
    this.state.ship(this);
  }

  public void completeOrder() {
    if (this.state == null) initBehavior();
    this.state.complete(this);
  }
}
