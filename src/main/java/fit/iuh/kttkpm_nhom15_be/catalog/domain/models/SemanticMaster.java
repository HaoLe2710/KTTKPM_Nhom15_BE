package fit.iuh.kttkpm_nhom15_be.catalog.domain.models;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemanticMaster {
  private String id;
  private SemanticMasterKind kind;
  private String code;
  private String name;
  private String description;
  private String slug;
  private String logoUrl;
  private String normalizedName;
  private String inciName;
  private boolean isActive;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
