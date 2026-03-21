package fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.OptionValue;
import java.util.Optional;

public interface OptionValueRepository {
  Optional<OptionValue> findById(String id);
}
