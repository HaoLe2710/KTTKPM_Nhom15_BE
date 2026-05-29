package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Option;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.OptionJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.mappers.CatalogDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// 1. Interface Spring Data JPA
interface JpaOptionRepository extends JpaRepository<OptionJpaEntity, String> {
    boolean existsByCodeIgnoreCase(String code);
    Optional<OptionJpaEntity> findByCodeIgnoreCase(String code);
}

// 2. Lớp Implement interface của tầng Domain
@Repository
@RequiredArgsConstructor
public class OptionRepositoryImpl implements OptionRepository {

  private final JpaOptionRepository jpaOptionRepository;
  private final CatalogDataMapper catalogDataMapper;

  @Override
  public Option save(Option option) {
      OptionJpaEntity entity = catalogDataMapper.toJpaEntity(option);
      return catalogDataMapper.toDomainModel(jpaOptionRepository.save(entity));
  }

  @Override
  public Optional<Option> findById(String id) {
    return jpaOptionRepository.findById(id)
      .map(catalogDataMapper::toDomainModel);
  }

  @Override
  public Optional<Option> findByCode(String code) {
      return jpaOptionRepository.findByCodeIgnoreCase(code).map(catalogDataMapper::toDomainModel);
  }

  @Override
  public boolean existsByCode(String code) {
      return jpaOptionRepository.existsByCodeIgnoreCase(code);
  }

  @Override
  public void deleteById(String id) {
      jpaOptionRepository.deleteById(id);
  }

  @Override
  public List<Option> findAll() {
      return jpaOptionRepository.findAll().stream()
              .map(catalogDataMapper::toDomainModel)
              .collect(Collectors.toList());
  }
}
