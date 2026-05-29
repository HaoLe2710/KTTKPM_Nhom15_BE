package fit.iuh.kttkpm_nhom15_be.payments.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.payments.application.commands.CreatePaymentCommand;
import fit.iuh.kttkpm_nhom15_be.payments.application.dto.PaymentStatusResponse;
import fit.iuh.kttkpm_nhom15_be.payments.application.dto.PaymentTransactionResponse;
import fit.iuh.kttkpm_nhom15_be.payments.application.usecases.CreatePaymentUseCase;
import fit.iuh.kttkpm_nhom15_be.payments.application.usecases.HandlePaymentCallbackUseCase;
import fit.iuh.kttkpm_nhom15_be.payments.application.usecases.QueryPaymentStatusUseCase;
import fit.iuh.kttkpm_nhom15_be.payments.presentation.requests.CreatePaymentRequest;
import fit.iuh.kttkpm_nhom15_be.payments.presentation.requests.SepayWebhookRequest;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.advice.ApiSuccessMessage;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.advice.SkipSuccessEnvelope;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Tag(name = "Payments")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private static final String SEPAY_WEBHOOK_SECRET_HEADER = "X-Sepay-Secret";
    private static final String VNPAY_METHOD = "VNPAY";

    private final CreatePaymentUseCase createPaymentUseCase;
    private final QueryPaymentStatusUseCase queryPaymentStatusUseCase;
    private final HandlePaymentCallbackUseCase handlePaymentCallbackUseCase;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @PostMapping("/create")
    @ApiSuccessMessage("Khởi tạo giao dịch thanh toán thành công")
    public ResponseEntity<PaymentTransactionResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request,
                                                                    HttpServletRequest httpServletRequest) {
        PaymentTransactionResponse response = createPaymentUseCase.execute(CreatePaymentCommand.builder()
            .orderId(request.getOrderId())
            .clientIp(resolveClientIp(httpServletRequest))
            .build());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentTransactionResponse> getByTransactionId(@PathVariable String transactionId) {
        return ResponseEntity.ok(queryPaymentStatusUseCase.getByTransactionId(transactionId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentTransactionResponse> getByOrderId(@PathVariable String orderId) {
        return ResponseEntity.ok(queryPaymentStatusUseCase.getByOrderId(orderId));
    }

    @GetMapping("/vnpay-return")
    @SkipSuccessEnvelope
    public ResponseEntity<Void> handleVnpayReturn(@RequestParam Map<String, String> callbackParams) {
        PaymentStatusResponse paymentStatus = handlePaymentCallbackUseCase.handleVnpayReturn(callbackParams);
        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, buildFrontendPaymentResultUrl(paymentStatus))
            .build();
    }

    @PostMapping("/vnpay-ipn")
    @SkipSuccessEnvelope
    public ResponseEntity<Map<String, String>> handleVnpayIpn(@RequestParam Map<String, String> callbackParams) {
        return ResponseEntity.ok(handlePaymentCallbackUseCase.handleVnpayIpn(callbackParams));
    }

    @PostMapping("/sepay-webhook")
    @SkipSuccessEnvelope
    public ResponseEntity<PaymentStatusResponse> handleSepayWebhook(
        @RequestBody SepayWebhookRequest request,
        @RequestHeader(value = SEPAY_WEBHOOK_SECRET_HEADER, required = false) String webhookSecret
    ) {
        return ResponseEntity.ok(handlePaymentCallbackUseCase.handleSepayWebhook(request, webhookSecret));
    }

    @PatchMapping("/{transactionId}/mark-paid")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @ApiSuccessMessage("Cập nhật thanh toán COD thành công")
    public ResponseEntity<PaymentStatusResponse> markCodPaid(@PathVariable String transactionId) {
        return ResponseEntity.ok(handlePaymentCallbackUseCase.markCodPaid(transactionId));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String buildFrontendPaymentResultUrl(PaymentStatusResponse paymentStatus) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(normalizeBaseUrl(frontendBaseUrl))
            .path("/payment-result")
            .queryParam("orderId", paymentStatus.getOrderId())
            .queryParam("orderNo", resolveOrderNo(paymentStatus))
            .queryParam("method", VNPAY_METHOD)
            .queryParam("source", "vnpay-return");

        if (paymentStatus.getTransactionStatus() != null) {
            builder.queryParam("transactionStatus", paymentStatus.getTransactionStatus().name());
        }
        if (paymentStatus.getOrderPaymentStatus() != null && !paymentStatus.getOrderPaymentStatus().isBlank()) {
            builder.queryParam("orderPaymentStatus", paymentStatus.getOrderPaymentStatus());
        }
        if (paymentStatus.getMessage() != null && !paymentStatus.getMessage().isBlank()) {
            builder.queryParam("message", paymentStatus.getMessage());
        }
        if (paymentStatus.getTransactionRef() != null && !paymentStatus.getTransactionRef().isBlank()) {
            builder.queryParam("transactionRef", paymentStatus.getTransactionRef());
        }

        return builder.build().toUriString();
    }

    private String resolveOrderNo(PaymentStatusResponse paymentStatus) {
        if (paymentStatus.getOrderNo() != null && !paymentStatus.getOrderNo().isBlank()) {
            return paymentStatus.getOrderNo();
        }
        return paymentStatus.getTransactionRef();
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:5173";
        }

        String normalized = baseUrl.trim();
        return normalized.endsWith("/")
            ? normalized.substring(0, normalized.length() - 1)
            : normalized;
    }
}
