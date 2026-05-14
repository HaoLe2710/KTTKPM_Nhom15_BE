package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "brands")
@Getter
@Setter
@NoArgsConstructor
public class BrandJpaEntity extends AbstractSemanticMasterJpaEntity {
  @Column(nullable = false, unique = true)
  private String slug;

  @Column(name = "logo_url", length = 500)
  private String logoUrl;
}
