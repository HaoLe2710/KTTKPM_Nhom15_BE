package fit.iuh.kttkpm_nhom15_be.orders.infrastructure.persistence.entities;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingMode;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingProvider;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
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
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @CreationTimestamp
    @Column(updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String orderNo; private String userId;
    private BigDecimal subtotalAmount; private BigDecimal discountAmount;
    private BigDecimal shippingFee; private BigDecimal totalAmount;
    private String promotionId;
    private String promotionCode;

    @Enumerated(EnumType.STRING) private OrderStatus status;
    @Enumerated(EnumType.STRING) private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING) private PaymentStatus paymentStatus;

    private String shipFullName; private String shipPhone; private String shipAddress;
    private String shipCity; private String shipDistrict; private String shipWard;

    @Enumerated(EnumType.STRING) private ShippingMode shippingMode;
    @Enumerated(EnumType.STRING) private ShippingProvider shippingProvider;

    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb")
    private Map<String, Object> shippingMeta;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemJpaEntity> items;
}
