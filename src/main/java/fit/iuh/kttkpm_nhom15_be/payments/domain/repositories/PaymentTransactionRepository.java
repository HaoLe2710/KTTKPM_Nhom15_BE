package fit.iuh.kttkpm_nhom15_be.payments.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTransaction;

import java.util.Optional;

public interface PaymentTransactionRepository {
    PaymentTransaction save(PaymentTransaction paymentTransaction);
    Optional<PaymentTransaction> findById(String id);
    Optional<PaymentTransaction> findByOrderId(String orderId);
    Optional<PaymentTransaction> findByTransactionRef(String transactionRef);
    boolean existsByTransactionRef(String transactionRef);
}
