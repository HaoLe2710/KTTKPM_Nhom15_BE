package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "option_values")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE option_values SET is_active = false WHERE id = ?")
// 2. Tự động lọc các bản ghi is_active = true khi Select
@SQLRestriction("is_active = true")
public class OptionValueJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "option_id")
    private OptionJpaEntity option;
    private String value;
    private boolean isActive;
}