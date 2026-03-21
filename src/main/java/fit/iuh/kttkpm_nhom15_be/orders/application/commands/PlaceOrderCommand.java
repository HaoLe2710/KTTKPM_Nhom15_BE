package fit.iuh.kttkpm_nhom15_be.orders.application.commands;

import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingMode;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingProvider;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderCommand {
    private String userId;
    
    // Thông tin giao nhận
    private String shipFullName;
    private String shipPhone;
    private String shipAddress;
    private String shipCity;
    private String shipDistrict;
    private String shipWard;

    // Vận chuyển & Thanh toán
    private ShippingMode shippingMode;
    private ShippingProvider shippingProvider;
    private BigDecimal shippingFee;
    private PaymentMethod paymentMethod;
}