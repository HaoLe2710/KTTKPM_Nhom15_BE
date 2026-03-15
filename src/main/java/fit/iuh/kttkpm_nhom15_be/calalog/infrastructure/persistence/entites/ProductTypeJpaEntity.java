package fit.iuh.kttkpm_nhom15_be.calalog.infrastructure.persistence.entites;

import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_types")
@Getter
@Setter
@NoArgsConstructor
public class ProductTypeJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String code;
    private String name; // Không có audit timestamps theo SQL
}