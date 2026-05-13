package fit.iuh.kttkpm_nhom15_be.payments.application.usecases;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.payments.application.commands.CreatePaymentCommand;
import fit.iuh.kttkpm_nhom15_be.payments.application.dto.PaymentInitializationResult;
import fit.iuh.kttkpm_nhom15_be.payments.application.dto.PaymentTransactionResponse;
import fit.iuh.kttkpm_nhom15_be.payments.application.interfaces.PaymentProviderGateway;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentProvider;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTransaction;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTxnStatus;
import fit.iuh.kttkpm_nhom15_be.payments.domain.repositories.PaymentTransactionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CreatePaymentUseCaseTest {

    @Test
    void createPaymentForCodDoesNotReturnRedirectUrl() {
        Fixture fixture = new Fixture();
        fixture.stubOrder(PaymentMethod.COD);

        PaymentTransactionResponse response = fixture.useCase.execute(new CreatePaymentCommand("order-1", "127.0.0.1"));

        ArgumentCaptor<PaymentTransaction> captor = ArgumentCaptor.forClass(PaymentTransaction.class);
        Mockito.verify(fixture.paymentTransactionRepository).save(captor.capture());
        assertEquals(PaymentTxnStatus.PENDING, captor.getValue().getStatus());
        assertNull(response.getPaymentRedirectUrl());
    }

    @Test
    void createPaymentForVnpayReturnsRedirectUrl() {
        Fixture fixture = new Fixture();
        fixture.stubOrder(PaymentMethod.VNPAY);

        PaymentTransactionResponse response = fixture.useCase.execute(new CreatePaymentCommand("order-1", "127.0.0.1"));

        assertEquals("https://vnpay.example/pay", response.getPaymentRedirectUrl());
        assertEquals("VNPAY", response.getPaymentInfo().get("gateway"));
    }

    @Test
    void createPaymentForSepayReturnsTransferInfo() {
        Fixture fixture = new Fixture();
        fixture.stubOrder(PaymentMethod.SEPAY);

        PaymentTransactionResponse response = fixture.useCase.execute(new CreatePaymentCommand("order-1", "127.0.0.1"));

        assertEquals("123456789", response.getPaymentInfo().get("accountNumber"));
        assertEquals("https://qr.sepay.example/ORD-1000", response.getPaymentRedirectUrl());
    }

    private static class Fixture {
        private final OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        private final PaymentTransactionRepository paymentTransactionRepository = Mockito.mock(PaymentTransactionRepository.class);
        private final PaymentProviderGateway codGateway = new StubGateway(PaymentMethod.COD, PaymentProvider.COD, null, Map.of("instructions", "COD"));
        private final PaymentProviderGateway vnpayGateway = new StubGateway(PaymentMethod.VNPAY, PaymentProvider.VNPAY, "https://vnpay.example/pay", Map.of("gateway", "VNPAY"));
        private final PaymentProviderGateway sepayGateway = new StubGateway(PaymentMethod.SEPAY, PaymentProvider.SEPAY, "https://qr.sepay.example/ORD-1000", Map.of("accountNumber", "123456789"));
        private final CreatePaymentUseCase useCase = new CreatePaymentUseCase(
            orderRepository,
            paymentTransactionRepository,
            List.of(codGateway, vnpayGateway, sepayGateway)
        );

        private void stubOrder(PaymentMethod paymentMethod) {
            Order order = Order.builder()
                .id("order-1")
                .orderNo("ORD-1000")
                .paymentMethod(paymentMethod)
                .paymentStatus(PaymentStatus.UNPAID)
                .status(OrderStatus.CREATED)
                .totalAmount(BigDecimal.valueOf(250_000))
                .build();

            when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
            when(paymentTransactionRepository.findByOrderId("order-1")).thenReturn(Optional.empty());
            when(paymentTransactionRepository.existsByTransactionRef("ORD-1000")).thenReturn(false);
            when(paymentTransactionRepository.save(any())).thenAnswer(invocation -> {
                PaymentTransaction transaction = invocation.getArgument(0);
                transaction.setId("payment-1");
                return transaction;
            });
        }
    }

    private static class StubGateway implements PaymentProviderGateway {
        private final PaymentMethod paymentMethod;
        private final PaymentProvider provider;
        private final String redirectUrl;
        private final Map<String, Object> paymentInfo;

        private StubGateway(PaymentMethod paymentMethod,
                            PaymentProvider provider,
                            String redirectUrl,
                            Map<String, Object> paymentInfo) {
            this.paymentMethod = paymentMethod;
            this.provider = provider;
            this.redirectUrl = redirectUrl;
            this.paymentInfo = paymentInfo;
        }

        @Override
        public PaymentMethod supportedMethod() {
            return paymentMethod;
        }

        @Override
        public PaymentInitializationResult initialize(Order order, PaymentTransaction paymentTransaction, String clientIp) {
            Map<String, Object> rawPayload = new LinkedHashMap<>();
            rawPayload.put("paymentInfo", paymentInfo);
            if (redirectUrl != null) {
                rawPayload.put("paymentRedirectUrl", redirectUrl);
            }

            return new PaymentInitializationResult(
                provider,
                PaymentTxnStatus.PENDING,
                redirectUrl,
                paymentInfo,
                rawPayload
            );
        }
    }
}
