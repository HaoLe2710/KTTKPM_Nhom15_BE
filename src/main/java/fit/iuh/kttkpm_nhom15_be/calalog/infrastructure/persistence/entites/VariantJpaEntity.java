package fit.iuh.kttkpm_nhom15_be.calalog.infrastructure.persistence.entites;

import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "variants")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE variants SET is_active = false WHERE id = ?")
// 2. Tự động lọc các bản ghi is_active = true khi Select
@SQLRestriction("is_active = true")
public class VariantJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @CreationTimestamp
    @Column(updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id")
    private ProductJpaEntity product;
    private String sku;
    private BigDecimal price;
    private int stockQuantity;
    private boolean isActive;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VariantOptionJpaEntity> options;
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaJpaEntity> media;
}