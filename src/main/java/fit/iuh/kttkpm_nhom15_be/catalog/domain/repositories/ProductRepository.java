package fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Product;

import java.util.Optional;

public interface ProductRepository {
  Optional<Product> findById(String id);
}
