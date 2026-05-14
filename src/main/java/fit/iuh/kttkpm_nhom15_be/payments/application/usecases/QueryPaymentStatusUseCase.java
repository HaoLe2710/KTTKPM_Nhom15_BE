package fit.iuh.kttkpm_nhom15_be.payments.application.usecases;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.payments.application.dto.PaymentTransactionResponse;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTransaction;
import fit.iuh.kttkpm_nhom15_be.payments.domain.repositories.PaymentTransactionRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueryPaymentStatusUseCase {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public PaymentTransactionResponse getByTransactionId(String transactionId) {
        PaymentTransaction paymentTransaction = paymentTransactionRepository.findById(transactionId)
            .orElseThrow(() -> new ApiNotFoundException("Payment transaction not found: " + transactionId));
        Order order = loadOrder(paymentTransaction.getOrderId());
        return PaymentTransactionResponse.from(paymentTransaction, order.getPaymentStatus());
    }

    @Transactional(readOnly = true)
    public PaymentTransactionResponse getByOrderId(String orderId) {
        PaymentTransaction paymentTransaction = paymentTransactionRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ApiNotFoundException("Payment transaction not found for order: " + orderId));
        Order order = loadOrder(orderId);
        return PaymentTransactionResponse.from(paymentTransaction, order.getPaymentStatus());
    }

    private Order loadOrder(String orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new ApiNotFoundException("Order not found: " + orderId));
    }
}
