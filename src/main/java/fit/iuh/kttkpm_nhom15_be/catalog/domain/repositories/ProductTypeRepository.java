package fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.ProductType;
import java.util.List;
import java.util.Optional;

public interface ProductTypeRepository {
    ProductType save(ProductType productType);
    Optional<ProductType> findById(String id);
    Optional<ProductType> findByCode(String code);
    boolean existsByCode(String code);
    void deleteById(String id);
    List<ProductType> findAll();
}
