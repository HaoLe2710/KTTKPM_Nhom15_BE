package fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Variant;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VariantRepository {
  Optional<Variant> findById(String id);
  Variant save(Variant variant);
  void patchPriceAndStock(String id, BigDecimal price, int addedStock);
  long countExistingByIds(List<String> ids);
}
