package fit.iuh.kttkpm_nhom15_be.payments.infrastructure.providers;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.payments.application.dto.PaymentInitializationResult;
import fit.iuh.kttkpm_nhom15_be.payments.application.dto.VnpayCallbackResult;
import fit.iuh.kttkpm_nhom15_be.payments.application.interfaces.PaymentProviderGateway;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentProvider;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTransaction;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTxnStatus;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class VnpayPaymentProvider implements PaymentProviderGateway {

    private static final DateTimeFormatter VNP_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final ZoneId VNPAY_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final VnpayProperties properties;

    public VnpayPaymentProvider(VnpayProperties properties) {
        this.properties = properties;
    }

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.VNPAY;
    }

    @Override
    public PaymentInitializationResult initialize(Order order, PaymentTransaction paymentTransaction, String clientIp) {
        validateConfiguration();
        String tmnCode = normalize(properties.getTmnCode());
        String payUrl = normalize(properties.getPayUrl());
        String returnUrl = normalize(properties.getReturnUrl());

        LocalDateTime now = LocalDateTime.now(VNPAY_TIME_ZONE);
        LocalDateTime expireAt = now.plusMinutes(15);

        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", properties.getVersion());
        vnpParams.put("vnp_Command", properties.getCommand());
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Amount", paymentTransaction.getAmount()
            .multiply(BigDecimal.valueOf(100))
            .setScale(0, RoundingMode.HALF_UP)
            .toPlainString());
        vnpParams.put("vnp_CreateDate", VNP_DATE_FORMATTER.format(now));
        vnpParams.put("vnp_CurrCode", properties.getCurrCode());
        vnpParams.put("vnp_IpAddr", resolveIp(clientIp));
        vnpParams.put("vnp_Locale", properties.getLocale());
        vnpParams.put("vnp_OrderInfo", "Thanh toán đơn hàng " + order.getOrderNo());
        vnpParams.put("vnp_OrderType", properties.getOrderType());
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_TxnRef", paymentTransaction.getTxnRef());
        vnpParams.put("vnp_ExpireDate", VNP_DATE_FORMATTER.format(expireAt));

        String queryString = buildQueryString(vnpParams);
        String secureHash = buildSecureHash(vnpParams);
        String separator = payUrl.contains("?") ? "&" : "?";
        String paymentUrl = payUrl + separator + queryString + "&vnp_SecureHash=" + secureHash;

        Map<String, Object> paymentInfo = new LinkedHashMap<>();
        paymentInfo.put("gateway", PaymentProvider.VNPAY.name());
        paymentInfo.put("transactionRef", paymentTransaction.getTxnRef());
        paymentInfo.put("expiresAt", VNP_DATE_FORMATTER.format(expireAt));

        Map<String, Object> rawPayload = new LinkedHashMap<>();
        rawPayload.put("paymentRedirectUrl", paymentUrl);
        rawPayload.put("paymentInfo", paymentInfo);
        rawPayload.put("requestParams", new LinkedHashMap<>(vnpParams));

        return new PaymentInitializationResult(
            PaymentProvider.VNPAY,
            PaymentTxnStatus.PENDING,
            paymentUrl,
            paymentInfo,
            rawPayload
        );
    }

    public VnpayCallbackResult verifyCallback(Map<String, String> callbackParams) {
        if (callbackParams == null || callbackParams.isEmpty()) {
            throw new ApiValidationException("Missing VNPAY callback parameters.");
        }

        String actualHash = callbackParams.get("vnp_SecureHash");
        if (actualHash == null || actualHash.isBlank()) {
            throw new ApiValidationException("Missing VNPAY secure hash.");
        }

        Map<String, String> hashInput = new TreeMap<>();
        callbackParams.forEach((key, value) -> {
            if (value != null
                && !value.isBlank()
                && !"vnp_SecureHash".equals(key)
                && !"vnp_SecureHashType".equals(key)) {
                hashInput.put(key, value);
            }
        });

        String expectedHash = buildSecureHash(hashInput);
        if (!expectedHash.equalsIgnoreCase(actualHash)) {
            throw new ApiValidationException("Invalid VNPAY secure hash.");
        }

        String transactionRef = requireField(callbackParams, "vnp_TxnRef");
        String responseCode = callbackParams.getOrDefault("vnp_ResponseCode", "99");
        BigDecimal amount = parseAmount(callbackParams.get("vnp_Amount"));

        Map<String, Object> rawPayload = new LinkedHashMap<>();
        hashInput.forEach(rawPayload::put);
        rawPayload.put("vnp_SecureHashVerified", true);
        rawPayload.put("vnp_ResponseCode", responseCode);

        return new VnpayCallbackResult(
            transactionRef,
            amount,
            responseCode,
            mapResponseCodeToStatus(responseCode),
            rawPayload
        );
    }

    public String buildSecureHash(Map<String, String> fields) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(normalize(properties.getHashSecret()).getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKeySpec);
            byte[] hashBytes = hmac512.doFinal(buildQueryString(fields).getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hashBytes.length * 2);
            for (byte hashByte : hashBytes) {
                builder.append(String.format("%02x", hashByte));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new ApiValidationException("Unable to generate VNPAY secure hash.");
        }
    }

    private String buildQueryString(Map<String, String> fields) {
        return fields.entrySet().stream()
            .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
            .collect(Collectors.joining("&"));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String requireField(Map<String, String> params, String field) {
        String value = params.get(field);
        if (value == null || value.isBlank()) {
            throw new ApiValidationException("Missing VNPAY field: " + field);
        }
        return value;
    }

    private BigDecimal parseAmount(String rawAmount) {
        if (rawAmount == null || rawAmount.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(rawAmount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private PaymentTxnStatus mapResponseCodeToStatus(String responseCode) {
        return switch (responseCode) {
            case "00" -> PaymentTxnStatus.SUCCESS;
            case "24" -> PaymentTxnStatus.CANCELLED;
            case "11" -> PaymentTxnStatus.EXPIRED;
            default -> PaymentTxnStatus.FAILED;
        };
    }

    private String resolveIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return "127.0.0.1";
        }
        return clientIp.contains(",")
            ? clientIp.split(",")[0].trim()
            : clientIp.trim();
    }

    private void validateConfiguration() {
        if (isBlank(normalize(properties.getTmnCode()))
            || isBlank(normalize(properties.getHashSecret()))
            || isBlank(normalize(properties.getPayUrl()))
            || isBlank(normalize(properties.getReturnUrl()))) {
            throw new ApiValidationException("VNPAY configuration is incomplete.");
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
