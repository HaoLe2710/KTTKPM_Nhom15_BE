package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.MediaType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "media")
@Getter
@Setter
@NoArgsConstructor
public class MediaJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; // Không có updatedAt

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductJpaEntity product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private VariantJpaEntity variant;
    private String url;
    private String publicId;
    @Enumerated(EnumType.STRING)
    private MediaType type;
    private boolean isPrimary;
}