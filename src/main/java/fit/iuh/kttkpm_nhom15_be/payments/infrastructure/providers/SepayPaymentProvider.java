package fit.iuh.kttkpm_nhom15_be.payments.infrastructure.providers;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.payments.application.dto.PaymentInitializationResult;
import fit.iuh.kttkpm_nhom15_be.payments.application.interfaces.PaymentProviderGateway;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentProvider;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTransaction;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTxnStatus;
import fit.iuh.kttkpm_nhom15_be.payments.presentation.requests.SepayWebhookRequest;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiUnauthorizedException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SepayPaymentProvider implements PaymentProviderGateway {

    private final SepayProperties properties;

    public SepayPaymentProvider(SepayProperties properties) {
        this.properties = properties;
    }

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.SEPAY;
    }

    @Override
    public PaymentInitializationResult initialize(Order order, PaymentTransaction paymentTransaction, String clientIp) {
        validateConfiguration();

        String transferContent = paymentTransaction.getTxnRef();
        String qrUrl = buildQrUrl(paymentTransaction.getAmount(), transferContent);

        Map<String, Object> paymentInfo = new LinkedHashMap<>();
        paymentInfo.put("gateway", PaymentProvider.SEPAY.name());
        paymentInfo.put("bankCode", properties.getBankCode());
        paymentInfo.put("accountNumber", properties.getAccountNumber());
        paymentInfo.put("accountName", properties.getAccountName());
        paymentInfo.put("amount", paymentTransaction.getAmount());
        paymentInfo.put("transferContent", transferContent);
        paymentInfo.put("qrUrl", qrUrl);

        Map<String, Object> rawPayload = new LinkedHashMap<>();
        rawPayload.put("paymentRedirectUrl", qrUrl);
        rawPayload.put("paymentInfo", paymentInfo);

        return new PaymentInitializationResult(
            PaymentProvider.SEPAY,
            PaymentTxnStatus.PENDING,
            qrUrl,
            paymentInfo,
            rawPayload
        );
    }

    public void validateWebhookSecret(String providedSecret) {
        if (properties.getWebhookSecret() == null || properties.getWebhookSecret().isBlank()) {
            return;
        }

        if (providedSecret == null || !properties.getWebhookSecret().equals(providedSecret.trim())) {
            throw new ApiUnauthorizedException("Invalid SePay webhook secret.");
        }
    }

    public String extractTransactionReference(SepayWebhookRequest request) {
        String reference = firstNonBlank(
            request.getReferenceCode(),
            request.getCode(),
            request.getTransferContent(),
            request.getContent(),
            request.getDescription()
        );

        if (reference == null) {
            throw new ApiValidationException("Unable to resolve SePay transaction reference.");
        }
        return reference.trim();
    }

    public BigDecimal extractAmount(SepayWebhookRequest request) {
        BigDecimal amount = request.getTransferAmount() != null ? request.getTransferAmount() : request.getAmount();
        if (amount == null || amount.signum() <= 0) {
            throw new ApiValidationException("Invalid SePay transfer amount.");
        }
        return amount;
    }

    public Map<String, Object> extractRawPayload(SepayWebhookRequest request) {
        Map<String, Object> rawPayload = new LinkedHashMap<>();
        rawPayload.put("gateway", request.getGateway());
        rawPayload.put("transactionDate", request.getTransactionDate());
        rawPayload.put("accountNumber", request.getAccountNumber());
        rawPayload.put("subAccount", request.getSubAccount());
        rawPayload.put("referenceCode", request.getReferenceCode());
        rawPayload.put("code", request.getCode());
        rawPayload.put("content", request.getContent());
        rawPayload.put("transferContent", request.getTransferContent());
        rawPayload.put("description", request.getDescription());
        rawPayload.put("transferType", request.getTransferType());
        rawPayload.put("amount", request.getTransferAmount() != null ? request.getTransferAmount() : request.getAmount());
        rawPayload.put("gatewayTransactionId", request.getGatewayTransactionId());
        rawPayload.put("id", request.getId());
        return rawPayload;
    }

    private String buildQrUrl(BigDecimal amount, String transferContent) {
        String encodedContent = URLEncoder.encode(transferContent, StandardCharsets.UTF_8);
        String encodedAccountName = URLEncoder.encode(properties.getAccountName(), StandardCharsets.UTF_8);
        return "https://img.vietqr.io/image/"
            + properties.getBankCode()
            + "-"
            + properties.getAccountNumber()
            + "-compact2.png?amount="
            + amount.toPlainString()
            + "&addInfo="
            + encodedContent
            + "&accountName="
            + encodedAccountName;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private void validateConfiguration() {
        if (isBlank(properties.getBankCode())
            || isBlank(properties.getAccountNumber())
            || isBlank(properties.getAccountName())) {
            throw new ApiValidationException("SePay configuration is incomplete.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
