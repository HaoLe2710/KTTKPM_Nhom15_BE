package fit.iuh.kttkpm_nhom15_be.payments.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.payments.infrastructure.persistence.entities.PaymentTransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTransactionJpaRepository extends JpaRepository<PaymentTransactionJpaEntity, String> {
    Optional<PaymentTransactionJpaEntity> findTopByOrderIdOrderByCreatedAtDesc(String orderId);
    Optional<PaymentTransactionJpaEntity> findByTxnRef(String txnRef);
    boolean existsByTxnRef(String txnRef);
}
