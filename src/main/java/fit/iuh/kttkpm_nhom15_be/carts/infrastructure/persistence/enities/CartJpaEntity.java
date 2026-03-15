package fit.iuh.kttkpm_nhom15_be.carts.infrastructure.persistence.enities;

import fit.iuh.kttkpm_nhom15_be.carts.domain.models.CartStatus;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
public class CartJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @CreationTimestamp
    @Column(updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String userId;
    @Enumerated(EnumType.STRING) private CartStatus status;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItemJpaEntity> items;
}