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

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE products SET is_active = false WHERE id = ?")
// 2. Tự động lọc các bản ghi is_active = true khi Select
@SQLRestriction("is_active = true")
public class ProductJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String typeId;
    private String name;
    private String slug;
    private String descriptionMd;
    private boolean isCustomizable;
    private boolean isActive;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VariantJpaEntity> variants;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MediaJpaEntity> media;
}