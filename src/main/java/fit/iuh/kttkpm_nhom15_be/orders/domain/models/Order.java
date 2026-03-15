package fit.iuh.kttkpm_nhom15_be.orders.domain.models;

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
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    private String shipFullName;
    private String shipPhone;
    private String shipAddress;
    private String shipCity;
    private String shipDistrict;
    private String shipWard;

    private ShippingMode shippingMode;
    private ShippingProvider shippingProvider;
    private Map<String, Object> shippingMeta;

    private List<OrderItem> items;
}