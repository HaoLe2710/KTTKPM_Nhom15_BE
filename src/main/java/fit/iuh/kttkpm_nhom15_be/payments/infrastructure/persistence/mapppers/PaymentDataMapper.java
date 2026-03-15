package fit.iuh.kttkpm_nhom15_be.payments.infrastructure.persistence.mapppers;

import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTransaction;
import fit.iuh.kttkpm_nhom15_be.payments.infrastructure.persistence.entities.PaymentTransactionJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentDataMapper {
    PaymentTransactionJpaEntity toJpaEntity(PaymentTransaction domain);
    PaymentTransaction toDomainModel(PaymentTransactionJpaEntity entity);
}