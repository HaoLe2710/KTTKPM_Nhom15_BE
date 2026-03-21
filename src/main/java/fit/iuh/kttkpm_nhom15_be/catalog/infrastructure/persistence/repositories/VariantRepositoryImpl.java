package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Variant;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.VariantJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.mappers.CatalogDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 1. Interface Spring Data JPA thông thường
interface JpaVariantRepository extends JpaRepository<VariantJpaEntity, String> {
}

// 2. Lớp Implement interface của tầng Domain
@Repository
@RequiredArgsConstructor
public class VariantRepositoryImpl implements VariantRepository {

  private final JpaVariantRepository jpaVariantRepository;
  private final CatalogDataMapper catalogDataMapper;

  @Override
  public Optional<Variant> findById(String id) {
    return jpaVariantRepository.findById(id)
      .map(catalogDataMapper::toDomainModel);
  }
}
