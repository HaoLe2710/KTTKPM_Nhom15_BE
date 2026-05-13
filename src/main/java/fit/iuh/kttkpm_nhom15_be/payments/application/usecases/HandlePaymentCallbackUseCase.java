package fit.iuh.kttkpm_nhom15_be.payments.application.usecases;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.payments.application.dto.PaymentStatusResponse;
import fit.iuh.kttkpm_nhom15_be.payments.application.dto.VnpayCallbackResult;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTransaction;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTxnStatus;
import fit.iuh.kttkpm_nhom15_be.payments.domain.repositories.PaymentTransactionRepository;
import fit.iuh.kttkpm_nhom15_be.payments.infrastructure.providers.SepayPaymentProvider;
import fit.iuh.kttkpm_nhom15_be.payments.infrastructure.providers.VnpayPaymentProvider;
import fit.iuh.kttkpm_nhom15_be.payments.presentation.requests.SepayWebhookRequest;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiNotFoundException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class HandlePaymentCallbackUseCase {

    private static final Pattern ORDER_REFERENCE_PATTERN = Pattern.compile("(ORD-[A-Za-z0-9-]+)");

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final VnpayPaymentProvider vnpayPaymentProvider;
    private final SepayPaymentProvider sepayPaymentProvider;

    @Transactional
    public PaymentStatusResponse handleVnpayReturn(Map<String, String> callbackParams) {
        VnpayCallbackResult callbackResult = vnpayPaymentProvider.verifyCallback(callbackParams);
        return applyGatewayResult(
            callbackResult.transactionRef(),
            callbackResult.amount(),
            callbackResult.status(),
            callbackResult.rawPayload(),
            resolveMessage(callbackResult.status(), "VNPAY callback processed successfully.")
        );
    }

    @Transactional
    public Map<String, String> handleVnpayIpn(Map<String, String> callbackParams) {
        final VnpayCallbackResult callbackResult;
        try {
            callbackResult = vnpayPaymentProvider.verifyCallback(callbackParams);
        } catch (ApiValidationException ex) {
            return buildVnpayIpnResponse("97", "Invalid signature");
        }

        PaymentTransaction paymentTransaction = paymentTransactionRepository.findByTransactionRef(callbackResult.transactionRef())
            .orElse(null);
        if (paymentTransaction == null) {
            return buildVnpayIpnResponse("01", "Order not found");
        }

        if (!amountMatches(paymentTransaction.getAmount(), callbackResult.amount())) {
            return buildVnpayIpnResponse("04", "Invalid amount");
        }

        if (paymentTransaction.getStatus() == PaymentTxnStatus.SUCCESS) {
            return buildVnpayIpnResponse("02", "Order already confirmed");
        }

        applyGatewayResult(
            paymentTransaction,
            callbackResult.amount(),
            callbackResult.status(),
            callbackResult.rawPayload(),
            resolveMessage(callbackResult.status(), "VNPAY IPN processed successfully.")
        );
        return buildVnpayIpnResponse("00", "Confirm Success");
    }

    @Transactional
    public PaymentStatusResponse handleSepayWebhook(SepayWebhookRequest request, String providedSecret) {
        sepayPaymentProvider.validateWebhookSecret(providedSecret);

        String transactionReference = sepayPaymentProvider.extractTransactionReference(request);
        PaymentTransaction paymentTransaction = resolveByReference(transactionReference)
            .orElseThrow(() -> {
                log.warn("Ignoring SePay webhook because transaction reference was not found.");
                return new ApiNotFoundException("Payment transaction not found for SePay webhook.");
            });

        BigDecimal paidAmount = sepayPaymentProvider.extractAmount(request);
        if (!amountMatches(paymentTransaction.getAmount(), paidAmount)) {
            log.warn("Ignoring SePay webhook because amount mismatch for transactionRef={}", paymentTransaction.getTxnRef());
            throw new ApiValidationException("SePay webhook amount mismatch.");
        }

        return applyGatewayResult(
            paymentTransaction,
            paidAmount,
            PaymentTxnStatus.SUCCESS,
            sepayPaymentProvider.extractRawPayload(request),
            "SePay payment confirmed successfully."
        );
    }

    @Transactional
    public PaymentStatusResponse markCodPaid(String transactionId) {
        PaymentTransaction paymentTransaction = paymentTransactionRepository.findById(transactionId)
            .orElseThrow(() -> new ApiNotFoundException("Payment transaction not found: " + transactionId));

        if (paymentTransaction.getMethod() != PaymentMethod.COD) {
            throw new ApiValidationException("Only COD transactions can be marked as paid manually.");
        }

        return applyGatewayResult(
            paymentTransaction,
            paymentTransaction.getAmount(),
            PaymentTxnStatus.SUCCESS,
            Map.of("codMarkedPaidAt", LocalDateTime.now().toString()),
            "COD payment marked as paid."
        );
    }

    private PaymentStatusResponse applyGatewayResult(String transactionRef,
                                                     BigDecimal paidAmount,
                                                     PaymentTxnStatus newStatus,
                                                     Map<String, Object> providerPayload,
                                                     String successMessage) {
        PaymentTransaction paymentTransaction = paymentTransactionRepository.findByTransactionRef(transactionRef)
            .orElseThrow(() -> new ApiNotFoundException("Payment transaction not found: " + transactionRef));
        return applyGatewayResult(paymentTransaction, paidAmount, newStatus, providerPayload, successMessage);
    }

    private PaymentStatusResponse applyGatewayResult(PaymentTransaction paymentTransaction,
                                                     BigDecimal paidAmount,
                                                     PaymentTxnStatus newStatus,
                                                     Map<String, Object> providerPayload,
                                                     String successMessage) {
        Order order = orderRepository.findById(paymentTransaction.getOrderId())
            .orElseThrow(() -> new ApiNotFoundException("Order not found: " + paymentTransaction.getOrderId()));

        if (!amountMatches(paymentTransaction.getAmount(), paidAmount)) {
            throw new ApiValidationException("Payment amount does not match the order total.");
        }

        if (paymentTransaction.getStatus() == PaymentTxnStatus.SUCCESS) {
            return PaymentStatusResponse.from(paymentTransaction, order, "Payment already confirmed.");
        }

        paymentTransaction.setRawPayload(mergePayload(paymentTransaction.getRawPayload(), providerPayload));
        paymentTransaction.setStatus(newStatus);

        if (newStatus == PaymentTxnStatus.SUCCESS) {
            order.setPaymentStatus(PaymentStatus.PAID);
            orderRepository.save(order);
        }

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(paymentTransaction);
        return PaymentStatusResponse.from(savedTransaction, order, newStatus == PaymentTxnStatus.SUCCESS
            ? successMessage
            : resolveMessage(newStatus, "Payment status updated."));
    }

    private Optional<PaymentTransaction> resolveByReference(String referenceText) {
        Optional<PaymentTransaction> exactMatch = paymentTransactionRepository.findByTransactionRef(referenceText);
        if (exactMatch.isPresent()) {
            return exactMatch;
        }

        Matcher matcher = ORDER_REFERENCE_PATTERN.matcher(referenceText);
        if (matcher.find()) {
            return paymentTransactionRepository.findByTransactionRef(matcher.group(1));
        }

        return Optional.empty();
    }

    private boolean amountMatches(BigDecimal expectedAmount, BigDecimal actualAmount) {
        return expectedAmount != null
            && actualAmount != null
            && expectedAmount.compareTo(actualAmount) == 0;
    }

    private Map<String, Object> mergePayload(Map<String, Object> existingPayload, Map<String, Object> newPayload) {
        Map<String, Object> mergedPayload = new LinkedHashMap<>();
        if (existingPayload != null) {
            mergedPayload.putAll(existingPayload);
        }
        if (newPayload != null) {
            mergedPayload.putAll(newPayload);
        }
        return mergedPayload;
    }

    private Map<String, String> buildVnpayIpnResponse(String responseCode, String message) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("RspCode", responseCode);
        response.put("Message", message);
        return response;
    }

    private String resolveMessage(PaymentTxnStatus status, String successMessage) {
        return switch (status) {
            case SUCCESS -> successMessage;
            case CANCELLED -> "Payment was cancelled by the customer.";
            case EXPIRED -> "Payment session has expired.";
            case FAILED -> "Payment failed.";
            case PENDING -> "Payment is pending.";
        };
    }
}
