package fit.iuh.kttkpm_nhom15_be.payments.application.usecases;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
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
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiNotFoundException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CreatePaymentUseCase {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final List<PaymentProviderGateway> paymentProviderGateways;

    @Transactional
    public PaymentTransactionResponse execute(CreatePaymentCommand command) {
        Order order = orderRepository.findById(command.getOrderId())
            .orElseThrow(() -> new ApiNotFoundException("Order not found: " + command.getOrderId()));
        return execute(order, command.getClientIp());
    }

    @Transactional
    public PaymentTransactionResponse execute(Order order, String clientIp) {
        PaymentTransaction existingTransaction = paymentTransactionRepository.findByOrderId(order.getId()).orElse(null);
        if (existingTransaction != null) {
            return PaymentTransactionResponse.from(existingTransaction, order.getPaymentStatus());
        }

        if (order.getPaymentMethod() == null) {
            throw new ApiValidationException("Order does not have a payment method.");
        }

        PaymentProviderGateway providerGateway = resolveGateway(order.getPaymentMethod());
        String transactionRef = generateTransactionRef(order);

        PaymentTransaction paymentTransaction = PaymentTransaction.builder()
            .orderId(order.getId())
            .provider(resolveProvider(order.getPaymentMethod()))
            .method(order.getPaymentMethod())
            .amount(order.getTotalAmount())
            .status(PaymentTxnStatus.PENDING)
            .txnRef(transactionRef)
            .rawPayload(new LinkedHashMap<>())
            .build();

        PaymentInitializationResult initializationResult = providerGateway.initialize(order, paymentTransaction, clientIp);
        paymentTransaction.setProvider(initializationResult.provider());
        paymentTransaction.setStatus(initializationResult.status());
        paymentTransaction.setRawPayload(enrichRawPayload(initializationResult.rawPayload(), initializationResult));

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(paymentTransaction);
        return PaymentTransactionResponse.from(savedTransaction, order.getPaymentStatus());
    }

    private PaymentProviderGateway resolveGateway(PaymentMethod paymentMethod) {
        return paymentProviderGateways.stream()
            .filter(provider -> provider.supports(paymentMethod))
            .findFirst()
            .orElseThrow(() -> new ApiValidationException("Unsupported payment method: " + paymentMethod));
    }

    private PaymentProvider resolveProvider(PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case COD -> PaymentProvider.COD;
            case VNPAY -> PaymentProvider.VNPAY;
            case SEPAY -> PaymentProvider.SEPAY;
        };
    }

    private String generateTransactionRef(Order order) {
        String baseRef = order.getOrderNo();
        if (!paymentTransactionRepository.existsByTransactionRef(baseRef)) {
            return baseRef;
        }

        int suffix = 1;
        while (paymentTransactionRepository.existsByTransactionRef(baseRef + "-" + suffix)) {
            suffix++;
        }
        return baseRef + "-" + suffix;
    }

    private Map<String, Object> enrichRawPayload(Map<String, Object> rawPayload, PaymentInitializationResult initializationResult) {
        Map<String, Object> normalized = new LinkedHashMap<>();
        if (rawPayload != null) {
            normalized.putAll(rawPayload);
        }
        if (initializationResult.paymentRedirectUrl() != null) {
            normalized.put("paymentRedirectUrl", initializationResult.paymentRedirectUrl());
        }
        if (initializationResult.paymentInfo() != null && !initializationResult.paymentInfo().isEmpty()) {
            normalized.put("paymentInfo", initializationResult.paymentInfo());
        }
        return normalized;
    }
}
