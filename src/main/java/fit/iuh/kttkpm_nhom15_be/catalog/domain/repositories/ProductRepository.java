package fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Product;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.ProductSummaryDTO;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepository {
  Optional<Product> findById(String id);
  boolean existsByTypeId(String typeId);
  Product save(Product product);
  Page<ProductSummaryDTO> findProductsSummary(String typeId, BigDecimal minPrice, BigDecimal maxPrice, int page, int size);
}
