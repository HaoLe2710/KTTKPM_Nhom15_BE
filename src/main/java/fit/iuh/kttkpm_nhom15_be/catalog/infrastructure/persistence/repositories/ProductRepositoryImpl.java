package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Product;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.ProductJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.mappers.CatalogDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.ProductSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

// 1. Interface Spring Data JPA thông thường
interface JpaProductRepository extends JpaRepository<ProductJpaEntity, String> {
    
    @Query("SELECT new fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.ProductSummaryDTO(" +
           "p.id, p.typeId, p.name, p.slug, MIN(v.price), SUM(v.stockQuantity)) " +
           "FROM ProductJpaEntity p LEFT JOIN p.variants v " +
           "WHERE (:typeId IS NULL OR p.typeId = :typeId) " +
           "AND p.isActive = true " +
           "GROUP BY p.id, p.typeId, p.name, p.slug " +
           "HAVING (:minPrice IS NULL OR MIN(v.price) >= :minPrice) " +
           "AND (:maxPrice IS NULL OR MIN(v.price) <= :maxPrice)")
    Page<ProductSummaryDTO> findProductSummaries(@Param("typeId") String typeId,
                                                 @Param("minPrice") BigDecimal minPrice,
                                                 @Param("maxPrice") BigDecimal maxPrice,
                                                 Pageable pageable);
}

// 2. Lớp Implement interface của tầng Domain
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

  private final JpaProductRepository jpaProductRepository;
  private final CatalogDataMapper catalogDataMapper;

  @Override
  public Optional<Product> findById(String id) {
    return jpaProductRepository.findById(id)
      .map(catalogDataMapper::toDomainModel);
  }

  @Override
  public Product save(Product product) {
      ProductJpaEntity entity = catalogDataMapper.toJpaEntity(product);
      return catalogDataMapper.toDomainModel(jpaProductRepository.save(entity));
  }

  @Override
  public Page<ProductSummaryDTO> findProductsSummary(String typeId, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
      return jpaProductRepository.findProductSummaries(typeId, minPrice, maxPrice, org.springframework.data.domain.PageRequest.of(page, size));
  }
}
