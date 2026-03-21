package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.OptionValue;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionValueRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.OptionValueJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.mappers.CatalogDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 1. Interface Spring Data JPA thông thường
interface JpaOptionValueRepository extends JpaRepository<OptionValueJpaEntity, String> {
}

// 2. Lớp Implement interface của tầng Domain
@Repository
@RequiredArgsConstructor
public class OptionValueRepositoryImpl implements OptionValueRepository {

  private final JpaOptionValueRepository jpaOptionValueRepository;
  private final CatalogDataMapper catalogDataMapper;

  @Override
  public Optional<OptionValue> findById(String id) {
    return jpaOptionValueRepository.findById(id)
      .map(catalogDataMapper::toDomainModel);
  }
}
