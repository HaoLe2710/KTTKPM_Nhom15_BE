package fit.iuh.kttkpm_nhom15_be.payments.application.dto;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentProvider;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTransaction;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTxnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionResponse {
    private String transactionId;
    private String orderId;
    private PaymentProvider provider;
    private PaymentMethod method;
    private BigDecimal amount;
    private PaymentTxnStatus status;
    private PaymentStatus orderPaymentStatus;
    private String transactionRef;
    private String paymentRedirectUrl;
    private Map<String, Object> paymentInfo;

    public static PaymentTransactionResponse from(PaymentTransaction transaction, PaymentStatus orderPaymentStatus) {
        Map<String, Object> rawPayload = transaction.getRawPayload() == null
            ? Map.of()
            : transaction.getRawPayload();

        return PaymentTransactionResponse.builder()
            .transactionId(transaction.getId())
            .orderId(transaction.getOrderId())
            .provider(transaction.getProvider())
            .method(transaction.getMethod())
            .amount(transaction.getAmount())
            .status(transaction.getStatus())
            .orderPaymentStatus(orderPaymentStatus)
            .transactionRef(transaction.getTxnRef())
            .paymentRedirectUrl(extractString(rawPayload.get("paymentRedirectUrl")))
            .paymentInfo(extractMap(rawPayload.get("paymentInfo")))
            .build();
    }

    private static String extractString(Object value) {
        return value instanceof String str && !str.isBlank() ? str : null;
    }

    private static Map<String, Object> extractMap(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }

        Map<String, Object> normalized = new LinkedHashMap<>();
        map.forEach((key, entryValue) -> normalized.put(String.valueOf(key), entryValue));
        return normalized;
    }
}
