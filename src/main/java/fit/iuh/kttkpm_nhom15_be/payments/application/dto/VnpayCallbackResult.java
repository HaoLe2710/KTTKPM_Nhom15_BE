package fit.iuh.kttkpm_nhom15_be.payments.application.dto;

import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTxnStatus;

import java.math.BigDecimal;
import java.util.Map;

public record VnpayCallbackResult(
    String transactionRef,
    BigDecimal amount,
    String responseCode,
    PaymentTxnStatus status,
    Map<String, Object> rawPayload
) {
}
