package fit.iuh.kttkpm_nhom15_be.reviews.infrastructure.persistence.entities;


import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "reviews",
    uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "productId"})
)
@Getter @Setter @NoArgsConstructor
public class ReviewJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; // SQL chỉ có created_at

    private String userId;
    private String productId;
    private String orderId;
    private int rating;
    private String content;
}