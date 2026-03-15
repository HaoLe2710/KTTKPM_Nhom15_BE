package fit.iuh.kttkpm_nhom15_be.users.infrastructure.persistence.entities;

import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.persistence.BaseJpaEntity;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.UserRole;
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
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE users SET is_active = false WHERE id = ?")
// 2. Tự động lọc các bản ghi is_active = true khi Select
@SQLRestriction("is_active = true")
public class UserJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @CreationTimestamp
    @Column(updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String email;
    private String phone;
    private String password;
    private String fullName;
    private String avatarUrl;
    @Enumerated(EnumType.STRING) private UserRole role;
    private boolean isActive = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressJpaEntity> addresses;
}

