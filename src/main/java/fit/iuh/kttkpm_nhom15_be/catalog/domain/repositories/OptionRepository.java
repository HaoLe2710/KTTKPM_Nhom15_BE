package fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Option;
import java.util.Optional;

public interface OptionRepository {
  Optional<Option> findById(String id);
}
