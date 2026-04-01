package fit.iuh.kttkpm_nhom15_be.users.infrastructure.persistence.entities;

import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.persistence.BaseJpaEntity;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
// Sử dụng Soft Delete theo logic của sếp
@SQLDelete(sql = "UPDATE users SET is_active = false WHERE id = ?")
@SQLRestriction("is_active = true")
public class UserJpaEntity extends BaseJpaEntity { // Kế thừa từ BaseJpaEntity của Leader

    // Xóa id, createdAt, updatedAt vì đã có trong BaseJpaEntity

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String phone;

    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "is_active")
    private boolean isActive = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressJpaEntity> addresses;
}