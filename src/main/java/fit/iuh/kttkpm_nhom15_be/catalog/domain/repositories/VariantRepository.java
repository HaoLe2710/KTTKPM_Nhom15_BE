package fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Variant;

import java.util.Optional;

public interface VariantRepository {
  Optional<Variant> findById(String id);
}
