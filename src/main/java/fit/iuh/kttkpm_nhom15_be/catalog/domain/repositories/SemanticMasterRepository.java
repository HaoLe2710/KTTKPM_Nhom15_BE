package fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.SemanticMaster;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.SemanticMasterKind;
import java.util.List;
import java.util.Optional;

public interface SemanticMasterRepository {
  SemanticMaster save(SemanticMaster semanticMaster);

  Optional<SemanticMaster> findById(SemanticMasterKind kind, String id);

  Optional<SemanticMaster> findByCode(SemanticMasterKind kind, String code);

  List<SemanticMaster> findAll(SemanticMasterKind kind);

  List<SemanticMaster> findAllActive(SemanticMasterKind kind);

  boolean existsByCode(SemanticMasterKind kind, String code);

  Optional<SemanticMaster> findBrandBySlug(String slug);

  boolean existsBrandBySlug(String slug);
}
