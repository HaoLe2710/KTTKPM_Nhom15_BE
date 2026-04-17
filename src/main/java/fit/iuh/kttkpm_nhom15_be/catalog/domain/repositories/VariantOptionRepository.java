package fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.VariantOption;
import java.util.List;

public interface VariantOptionRepository {
    VariantOption save(VariantOption variantOption);
    boolean existsByOptionValueIds(List<String> optionValueIds);
}
