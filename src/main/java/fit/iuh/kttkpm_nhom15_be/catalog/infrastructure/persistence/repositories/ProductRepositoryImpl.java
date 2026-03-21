package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Product;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.ProductJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.mappers.CatalogDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 1. Interface Spring Data JPA thông thường
interface JpaProductRepository extends JpaRepository<ProductJpaEntity, String> {
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
}
