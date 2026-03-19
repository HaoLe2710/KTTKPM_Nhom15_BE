package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "variant_options")
@Getter
@Setter
@NoArgsConstructor
public class VariantOptionJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private VariantJpaEntity variant;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_value_id")
    private OptionValueJpaEntity optionValue;
}