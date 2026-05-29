package fit.iuh.kttkpm_nhom15_be.payments.infrastructure.providers;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.payments.application.dto.PaymentInitializationResult;
import fit.iuh.kttkpm_nhom15_be.payments.application.dto.VnpayCallbackResult;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTransaction;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTxnStatus;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VnpayPaymentProviderTest {

    @Test
    void initializeBuildsPaymentRedirectUrl() {
        VnpayPaymentProvider provider = new VnpayPaymentProvider(properties());
        Order order = Order.builder().orderNo("ORD-1000").build();
        PaymentTransaction transaction = PaymentTransaction.builder()
            .txnRef("ORD-1000")
            .amount(BigDecimal.valueOf(150_000))
            .method(PaymentMethod.VNPAY)
            .build();

        PaymentInitializationResult result = provider.initialize(order, transaction, "127.0.0.1");

        assertTrue(result.paymentRedirectUrl().contains("vnp_TxnRef=ORD-1000"));
        assertTrue(result.paymentRedirectUrl().contains("vnp_TmnCode=TMNCODE"));
    }

    @Test
    void verifyCallbackAcceptsValidSecureHash() {
        VnpayPaymentProvider provider = new VnpayPaymentProvider(properties());
        TreeMap<String, String> params = baseParams();
        params.put("vnp_SecureHash", provider.buildSecureHash(params));

        VnpayCallbackResult result = provider.verifyCallback(params);

        assertEquals("ORD-1000", result.transactionRef());
        assertEquals(PaymentTxnStatus.SUCCESS, result.status());
    }

    @Test
    void verifyCallbackRejectsInvalidSecureHash() {
        VnpayPaymentProvider provider = new VnpayPaymentProvider(properties());
        Map<String, String> params = new TreeMap<>(baseParams());
        params.put("vnp_SecureHash", "invalid");

        assertThrows(ApiValidationException.class, () -> provider.verifyCallback(params));
    }

    private static TreeMap<String, String> baseParams() {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("vnp_Amount", "15000000");
        params.put("vnp_Command", "pay");
        params.put("vnp_CreateDate", "20260509120000");
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_Locale", "vn");
        params.put("vnp_OrderInfo", "Thanh toán đơn hàng ORD-1000");
        params.put("vnp_OrderType", "other");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_ReturnUrl", "http://localhost:8080/api/v1/payments/vnpay-return");
        params.put("vnp_TmnCode", "TMNCODE");
        params.put("vnp_TxnRef", "ORD-1000");
        params.put("vnp_Version", "2.1.0");
        return params;
    }

    private static VnpayProperties properties() {
        VnpayProperties properties = new VnpayProperties();
        properties.setTmnCode("TMNCODE");
        properties.setHashSecret("SECRETKEY1234567890SECRETKEY1234567890");
        properties.setPayUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        properties.setReturnUrl("http://localhost:8080/api/v1/payments/vnpay-return");
        properties.setLocale("vn");
        properties.setOrderType("other");
        return properties;
    }
}
