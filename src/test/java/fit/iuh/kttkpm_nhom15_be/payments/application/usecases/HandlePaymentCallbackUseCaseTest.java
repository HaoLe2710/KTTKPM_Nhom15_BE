package fit.iuh.kttkpm_nhom15_be.payments.application.usecases;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.payments.application.dto.PaymentStatusResponse;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentProvider;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTransaction;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTxnStatus;
import fit.iuh.kttkpm_nhom15_be.payments.domain.repositories.PaymentTransactionRepository;
import fit.iuh.kttkpm_nhom15_be.payments.infrastructure.providers.SepayPaymentProvider;
import fit.iuh.kttkpm_nhom15_be.payments.infrastructure.providers.SepayProperties;
import fit.iuh.kttkpm_nhom15_be.payments.infrastructure.providers.VnpayPaymentProvider;
import fit.iuh.kttkpm_nhom15_be.payments.infrastructure.providers.VnpayProperties;
import fit.iuh.kttkpm_nhom15_be.payments.presentation.requests.SepayWebhookRequest;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HandlePaymentCallbackUseCaseTest {

    @Test
    void vnpayCallbackSuccessMarksOrderPaid() {
        Fixture fixture = new Fixture();

        PaymentStatusResponse response = fixture.useCase.handleVnpayReturn(fixture.validVnpayParams("00"));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(fixture.orderRepository).save(orderCaptor.capture());
        assertEquals(PaymentStatus.PAID, orderCaptor.getValue().getPaymentStatus());
        assertEquals(PaymentTxnStatus.SUCCESS, response.getTransactionStatus());
    }

    @Test
    void vnpayCallbackFailureKeepsOrderUnpaid() {
        Fixture fixture = new Fixture();

        PaymentStatusResponse response = fixture.useCase.handleVnpayReturn(fixture.validVnpayParams("24"));

        verify(fixture.orderRepository, never()).save(any());
        assertEquals(PaymentTxnStatus.CANCELLED, response.getTransactionStatus());
    }

    @Test
    void sepayWebhookSuccessMarksOrderPaid() {
        Fixture fixture = new Fixture();
        SepayWebhookRequest request = new SepayWebhookRequest();
        request.setTransferContent("ORD-1000");
        request.setTransferAmount(BigDecimal.valueOf(125_000));
        request.setGatewayTransactionId("bank-txn-1");

        PaymentStatusResponse response = fixture.useCase.handleSepayWebhook(request, "secret-value");

        assertEquals(PaymentTxnStatus.SUCCESS, response.getTransactionStatus());
        verify(fixture.orderRepository).save(any(Order.class));
    }

    @Test
    void sepayWebhookAmountMismatchThrowsValidationError() {
        Fixture fixture = new Fixture();
        SepayWebhookRequest request = new SepayWebhookRequest();
        request.setTransferContent("ORD-1000");
        request.setTransferAmount(BigDecimal.valueOf(100_000));

        assertThrows(ApiValidationException.class, () -> fixture.useCase.handleSepayWebhook(request, "secret-value"));
        verify(fixture.orderRepository, never()).save(any());
    }

    private static class Fixture {
        private final PaymentTransactionRepository paymentTransactionRepository = Mockito.mock(PaymentTransactionRepository.class);
        private final OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        private final VnpayPaymentProvider vnpayPaymentProvider = new VnpayPaymentProvider(vnpayProperties());
        private final SepayPaymentProvider sepayPaymentProvider = new SepayPaymentProvider(sepayProperties());
        private final HandlePaymentCallbackUseCase useCase = new HandlePaymentCallbackUseCase(
            paymentTransactionRepository,
            orderRepository,
            vnpayPaymentProvider,
            sepayPaymentProvider
        );

        private Fixture() {
            PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .id("payment-1")
                .orderId("order-1")
                .provider(PaymentProvider.VNPAY)
                .method(PaymentMethod.VNPAY)
                .amount(BigDecimal.valueOf(125_000))
                .status(PaymentTxnStatus.PENDING)
                .txnRef("ORD-1000")
                .rawPayload(Map.of())
                .build();
            Order order = Order.builder()
                .id("order-1")
                .orderNo("ORD-1000")
                .paymentStatus(PaymentStatus.UNPAID)
                .status(OrderStatus.CREATED)
                .build();

            when(paymentTransactionRepository.findByTransactionRef("ORD-1000")).thenReturn(Optional.of(paymentTransaction));
            when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
            when(paymentTransactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        }

        private Map<String, String> validVnpayParams(String responseCode) {
            TreeMap<String, String> params = new TreeMap<>();
            params.put("vnp_Amount", "12500000");
            params.put("vnp_Command", "pay");
            params.put("vnp_CreateDate", "20260509120000");
            params.put("vnp_CurrCode", "VND");
            params.put("vnp_IpAddr", "127.0.0.1");
            params.put("vnp_Locale", "vn");
            params.put("vnp_OrderInfo", "Thanh toán đơn hàng ORD-1000");
            params.put("vnp_OrderType", "other");
            params.put("vnp_ResponseCode", responseCode);
            params.put("vnp_ReturnUrl", "http://localhost:8080/api/v1/payments/vnpay-return");
            params.put("vnp_TmnCode", "TMNCODE");
            params.put("vnp_TxnRef", "ORD-1000");
            params.put("vnp_Version", "2.1.0");
            params.put("vnp_SecureHash", vnpayPaymentProvider.buildSecureHash(params));
            return params;
        }

        private static VnpayProperties vnpayProperties() {
            VnpayProperties properties = new VnpayProperties();
            properties.setTmnCode("TMNCODE");
            properties.setHashSecret("SECRETKEY1234567890SECRETKEY1234567890");
            properties.setPayUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
            properties.setReturnUrl("http://localhost:8080/api/v1/payments/vnpay-return");
            properties.setIpnUrl("http://localhost:8080/api/v1/payments/vnpay-ipn");
            properties.setLocale("vn");
            properties.setOrderType("other");
            return properties;
        }

        private static SepayProperties sepayProperties() {
            SepayProperties properties = new SepayProperties();
            properties.setWebhookSecret("secret-value");
            properties.setBankCode("MB");
            properties.setAccountNumber("123456789");
            properties.setAccountName("TEST ACCOUNT");
            return properties;
        }
    }
}
