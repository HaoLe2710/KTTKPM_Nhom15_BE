package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.OptionValue;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionValueRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.OptionJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.OptionValueJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// 1. Interface Spring Data JPA thông thường
interface JpaOptionValueRepository extends JpaRepository<OptionValueJpaEntity, String> {
    List<OptionValueJpaEntity> findByOptionId(String optionId);
    @Modifying
    void deleteByOptionId(String optionId);
}

// 2. Lớp Implement interface của tầng Domain
@Repository
@RequiredArgsConstructor
public class OptionValueRepositoryImpl implements OptionValueRepository {

  private final JpaOptionValueRepository jpaOptionValueRepository;
  private final JpaOptionRepository jpaOptionRepository;

  @Override
  public OptionValue save(OptionValue optionValue) {
      OptionJpaEntity optionEntity = jpaOptionRepository.findById(optionValue.getOptionId())
              .orElseThrow(() -> new IllegalArgumentException("Option khong ton tai: " + optionValue.getOptionId()));

      OptionValueJpaEntity entity = new OptionValueJpaEntity();
      entity.setId(optionValue.getId());
      entity.setOption(optionEntity);
      entity.setValue(optionValue.getValue());
      entity.setActive(optionValue.isActive());
      return toDomain(jpaOptionValueRepository.save(entity));
  }

  @Override
  public Optional<OptionValue> findById(String id) {
    return jpaOptionValueRepository.findById(id)
      .map(this::toDomain);
  }

  @Override
  public List<OptionValue> findAll() {
      return jpaOptionValueRepository.findAll().stream()
              .map(this::toDomain)
              .collect(Collectors.toList());
  }

  @Override
  public List<OptionValue> findByOptionId(String optionId) {
      return jpaOptionValueRepository.findByOptionId(optionId).stream()
              .map(this::toDomain)
              .collect(Collectors.toList());
  }

  @Override
  public void deleteByOptionId(String optionId) {
      jpaOptionValueRepository.deleteByOptionId(optionId);
  }

  private OptionValue toDomain(OptionValueJpaEntity entity) {
      return OptionValue.builder()
              .id(entity.getId())
              .optionId(entity.getOption() != null ? entity.getOption().getId() : null)
              .value(entity.getValue())
              .isActive(entity.isActive())
              .build();
  }
}
