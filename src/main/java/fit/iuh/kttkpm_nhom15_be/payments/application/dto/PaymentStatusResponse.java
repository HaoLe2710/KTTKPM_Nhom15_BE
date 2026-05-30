package fit.iuh.kttkpm_nhom15_be.payments.application.dto;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTransaction;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTxnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {
    private String transactionId;
    private String orderId;
    private String orderNo;
    private String transactionRef;
    private PaymentTxnStatus transactionStatus;
    private String orderStatus;
    private String orderPaymentStatus;
    private String paymentRedirectUrl;
    private Map<String, Object> paymentInfo;
    private String message;

    public static PaymentStatusResponse from(PaymentTransaction transaction, Order order, String message) {
        PaymentTransactionResponse transactionResponse = PaymentTransactionResponse.from(transaction, order.getPaymentStatus());
        return PaymentStatusResponse.builder()
            .transactionId(transactionResponse.getTransactionId())
            .orderId(transactionResponse.getOrderId())
            .orderNo(order.getOrderNo())
            .transactionRef(transactionResponse.getTransactionRef())
            .transactionStatus(transactionResponse.getStatus())
            .orderStatus(order.getStatus() != null ? order.getStatus().name() : null)
            .orderPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
            .paymentRedirectUrl(transactionResponse.getPaymentRedirectUrl())
            .paymentInfo(transactionResponse.getPaymentInfo())
            .message(message)
            .build();
    }
}
