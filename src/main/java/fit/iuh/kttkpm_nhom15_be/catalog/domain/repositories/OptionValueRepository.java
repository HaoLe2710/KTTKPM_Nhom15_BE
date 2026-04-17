package fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.OptionValue;
import java.util.List;
import java.util.Optional;

public interface OptionValueRepository {
  OptionValue save(OptionValue optionValue);
  Optional<OptionValue> findById(String id);
  List<OptionValue> findByOptionId(String optionId);
  void deleteByOptionId(String optionId);
  List<OptionValue> findAll();
}
