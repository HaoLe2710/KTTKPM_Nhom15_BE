package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.OptionValue;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionValueRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.OptionValueJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.mappers.CatalogDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
  public OptionValue save(OptionValue optionValue) {
      OptionValueJpaEntity entity = catalogDataMapper.toJpaEntity(optionValue);
      return catalogDataMapper.toDomainModel(jpaOptionValueRepository.save(entity));
  }

  @Override
  public Optional<OptionValue> findById(String id) {
    return jpaOptionValueRepository.findById(id)
      .map(catalogDataMapper::toDomainModel);
  }

  @Override
  public List<OptionValue> findAll() {
      return jpaOptionValueRepository.findAll().stream()
              .map(catalogDataMapper::toDomainModel)
              .collect(Collectors.toList());
  }
}
