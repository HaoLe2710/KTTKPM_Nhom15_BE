package fit.iuh.kttkpm_nhom15_be.payments.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTransaction;
import fit.iuh.kttkpm_nhom15_be.payments.domain.repositories.PaymentTransactionRepository;
import fit.iuh.kttkpm_nhom15_be.payments.infrastructure.persistence.entities.PaymentTransactionJpaEntity;
import fit.iuh.kttkpm_nhom15_be.payments.infrastructure.persistence.mapppers.PaymentDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentTransactionRepositoryImpl implements PaymentTransactionRepository {

    private final PaymentTransactionJpaRepository jpaPaymentTransactionRepository;
    private final PaymentDataMapper paymentDataMapper;

    @Override
    public PaymentTransaction save(PaymentTransaction paymentTransaction) {
        PaymentTransactionJpaEntity savedEntity = jpaPaymentTransactionRepository.save(
            paymentDataMapper.toJpaEntity(paymentTransaction)
        );
        return paymentDataMapper.toDomainModel(savedEntity);
    }

    @Override
    public Optional<PaymentTransaction> findById(String id) {
        return jpaPaymentTransactionRepository.findById(id)
            .map(paymentDataMapper::toDomainModel);
    }

    @Override
    public Optional<PaymentTransaction> findByOrderId(String orderId) {
        return jpaPaymentTransactionRepository.findTopByOrderIdOrderByCreatedAtDesc(orderId)
            .map(paymentDataMapper::toDomainModel);
    }

    @Override
    public Optional<PaymentTransaction> findByTransactionRef(String transactionRef) {
        return jpaPaymentTransactionRepository.findByTxnRef(transactionRef)
            .map(paymentDataMapper::toDomainModel);
    }

    @Override
    public boolean existsByTransactionRef(String transactionRef) {
        return jpaPaymentTransactionRepository.existsByTxnRef(transactionRef);
    }
}
