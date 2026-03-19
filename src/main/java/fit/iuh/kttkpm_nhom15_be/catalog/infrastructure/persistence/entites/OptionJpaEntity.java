package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "options")
@Getter
@Setter
@NoArgsConstructor
public class OptionJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String code;
    private String name;
}