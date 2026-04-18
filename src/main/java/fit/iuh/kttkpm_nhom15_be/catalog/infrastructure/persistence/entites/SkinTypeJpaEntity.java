package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "skin_types")
@Getter
@Setter
@NoArgsConstructor
public class SkinTypeJpaEntity extends AbstractSemanticMasterJpaEntity {
}
