package fit.iuh.kttkpm_nhom15_be.users.infrastructure.persistence.entities;

import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
public class AddressJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @CreationTimestamp
    @Column(updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private UserJpaEntity user;

    private String fullName;
    private String phone;
    private String address;
    private String city;
    private String district;
    private String ward;
    private boolean isDefault;
}