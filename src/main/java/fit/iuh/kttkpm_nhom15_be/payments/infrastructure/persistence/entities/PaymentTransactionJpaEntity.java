package fit.iuh.kttkpm_nhom15_be.payments.infrastructure.persistence.entities;

import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentProvider;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTxnStatus;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "payment_transactions")
@Getter @Setter @NoArgsConstructor
public class PaymentTransactionJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String orderId;
    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private PaymentTxnStatus status;
    private String txnRef;

    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb")
    private Map<String, Object> rawPayload;
}