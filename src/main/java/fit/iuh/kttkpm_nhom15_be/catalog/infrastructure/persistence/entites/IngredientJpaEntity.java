package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@NoArgsConstructor
public class IngredientJpaEntity extends AbstractSemanticMasterJpaEntity {
  @Column(name = "normalized_name")
  private String normalizedName;

  @Column(name = "inci_name")
  private String inciName;
}
