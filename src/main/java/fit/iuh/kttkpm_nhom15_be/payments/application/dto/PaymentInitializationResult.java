package fit.iuh.kttkpm_nhom15_be.payments.application.dto;

import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentProvider;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTxnStatus;

import java.util.Map;

public record PaymentInitializationResult(
    PaymentProvider provider,
    PaymentTxnStatus status,
    String paymentRedirectUrl,
    Map<String, Object> paymentInfo,
    Map<String, Object> rawPayload
) {
}
