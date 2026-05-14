package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.SemanticMaster;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.SemanticMasterKind;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.SemanticMasterRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.AbstractSemanticMasterJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.BrandJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.ConcernJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.IngredientJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.SkinTypeJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.TagJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

interface JpaBrandSemanticMasterRepository extends JpaRepository<BrandJpaEntity, String> {
  Optional<BrandJpaEntity> findByCodeIgnoreCase(String code);

  List<BrandJpaEntity> findAllByOrderByNameAsc();

  List<BrandJpaEntity> findAllByIsActiveTrueOrderByNameAsc();

  Optional<BrandJpaEntity> findBySlugIgnoreCase(String slug);

  boolean existsByCodeIgnoreCase(String code);

  boolean existsBySlugIgnoreCase(String slug);
}

interface JpaIngredientSemanticMasterRepository extends JpaRepository<IngredientJpaEntity, String> {
  Optional<IngredientJpaEntity> findByCodeIgnoreCase(String code);

  List<IngredientJpaEntity> findAllByOrderByNameAsc();

  List<IngredientJpaEntity> findAllByIsActiveTrueOrderByNameAsc();

  boolean existsByCodeIgnoreCase(String code);
}

interface JpaSkinTypeSemanticMasterRepository extends JpaRepository<SkinTypeJpaEntity, String> {
  Optional<SkinTypeJpaEntity> findByCodeIgnoreCase(String code);

  List<SkinTypeJpaEntity> findAllByOrderByNameAsc();

  List<SkinTypeJpaEntity> findAllByIsActiveTrueOrderByNameAsc();

  boolean existsByCodeIgnoreCase(String code);
}

interface JpaConcernSemanticMasterRepository extends JpaRepository<ConcernJpaEntity, String> {
  Optional<ConcernJpaEntity> findByCodeIgnoreCase(String code);

  List<ConcernJpaEntity> findAllByOrderByNameAsc();

  List<ConcernJpaEntity> findAllByIsActiveTrueOrderByNameAsc();

  boolean existsByCodeIgnoreCase(String code);
}

interface JpaTagSemanticMasterRepository extends JpaRepository<TagJpaEntity, String> {
  Optional<TagJpaEntity> findByCodeIgnoreCase(String code);

  List<TagJpaEntity> findAllByOrderByNameAsc();

  List<TagJpaEntity> findAllByIsActiveTrueOrderByNameAsc();

  boolean existsByCodeIgnoreCase(String code);
}

@Repository
@RequiredArgsConstructor
public class SemanticMasterRepositoryImpl implements SemanticMasterRepository {

  private final JpaBrandSemanticMasterRepository brandRepository;
  private final JpaIngredientSemanticMasterRepository ingredientRepository;
  private final JpaSkinTypeSemanticMasterRepository skinTypeRepository;
  private final JpaConcernSemanticMasterRepository concernRepository;
  private final JpaTagSemanticMasterRepository tagRepository;

  @Override
  public SemanticMaster save(SemanticMaster semanticMaster) {
    return switch (semanticMaster.getKind()) {
      case BRAND -> toDomain(brandRepository.save(toBrandEntity(semanticMaster)));
      case INGREDIENT -> toDomain(ingredientRepository.save(toIngredientEntity(semanticMaster)));
      case SKIN_TYPE -> toDomain(skinTypeRepository.save(toSkinTypeEntity(semanticMaster)));
      case CONCERN -> toDomain(concernRepository.save(toConcernEntity(semanticMaster)));
      case TAG -> toDomain(tagRepository.save(toTagEntity(semanticMaster)));
    };
  }

  @Override
  public Optional<SemanticMaster> findById(SemanticMasterKind kind, String id) {
    return switch (kind) {
      case BRAND -> mapOptional(brandRepository.findById(id), this::toDomain);
      case INGREDIENT -> mapOptional(ingredientRepository.findById(id), this::toDomain);
      case SKIN_TYPE -> mapOptional(skinTypeRepository.findById(id), this::toDomain);
      case CONCERN -> mapOptional(concernRepository.findById(id), this::toDomain);
      case TAG -> mapOptional(tagRepository.findById(id), this::toDomain);
    };
  }

  @Override
  public Optional<SemanticMaster> findByCode(SemanticMasterKind kind, String code) {
    return switch (kind) {
      case BRAND -> mapOptional(brandRepository.findByCodeIgnoreCase(code), this::toDomain);
      case INGREDIENT -> mapOptional(ingredientRepository.findByCodeIgnoreCase(code), this::toDomain);
      case SKIN_TYPE -> mapOptional(skinTypeRepository.findByCodeIgnoreCase(code), this::toDomain);
      case CONCERN -> mapOptional(concernRepository.findByCodeIgnoreCase(code), this::toDomain);
      case TAG -> mapOptional(tagRepository.findByCodeIgnoreCase(code), this::toDomain);
    };
  }

  @Override
  public List<SemanticMaster> findAll(SemanticMasterKind kind) {
    return switch (kind) {
      case BRAND -> brandRepository.findAllByOrderByNameAsc().stream().map(this::toDomain).toList();
      case INGREDIENT -> ingredientRepository.findAllByOrderByNameAsc().stream().map(this::toDomain).toList();
      case SKIN_TYPE -> skinTypeRepository.findAllByOrderByNameAsc().stream().map(this::toDomain).toList();
      case CONCERN -> concernRepository.findAllByOrderByNameAsc().stream().map(this::toDomain).toList();
      case TAG -> tagRepository.findAllByOrderByNameAsc().stream().map(this::toDomain).toList();
    };
  }

  @Override
  public List<SemanticMaster> findAllActive(SemanticMasterKind kind) {
    return switch (kind) {
      case BRAND -> brandRepository.findAllByIsActiveTrueOrderByNameAsc().stream().map(this::toDomain).toList();
      case INGREDIENT -> ingredientRepository.findAllByIsActiveTrueOrderByNameAsc().stream().map(this::toDomain).toList();
      case SKIN_TYPE -> skinTypeRepository.findAllByIsActiveTrueOrderByNameAsc().stream().map(this::toDomain).toList();
      case CONCERN -> concernRepository.findAllByIsActiveTrueOrderByNameAsc().stream().map(this::toDomain).toList();
      case TAG -> tagRepository.findAllByIsActiveTrueOrderByNameAsc().stream().map(this::toDomain).toList();
    };
  }

  @Override
  public boolean existsByCode(SemanticMasterKind kind, String code) {
    return switch (kind) {
      case BRAND -> brandRepository.existsByCodeIgnoreCase(code);
      case INGREDIENT -> ingredientRepository.existsByCodeIgnoreCase(code);
      case SKIN_TYPE -> skinTypeRepository.existsByCodeIgnoreCase(code);
      case CONCERN -> concernRepository.existsByCodeIgnoreCase(code);
      case TAG -> tagRepository.existsByCodeIgnoreCase(code);
    };
  }

  @Override
  public Optional<SemanticMaster> findBrandBySlug(String slug) {
    return mapOptional(brandRepository.findBySlugIgnoreCase(slug), this::toDomain);
  }

  @Override
  public boolean existsBrandBySlug(String slug) {
    return brandRepository.existsBySlugIgnoreCase(slug);
  }

  private <T> Optional<SemanticMaster> mapOptional(Optional<T> source, Function<T, SemanticMaster> mapper) {
    return source.map(mapper);
  }

  private SemanticMaster toDomain(BrandJpaEntity entity) {
    return baseBuilder(entity, SemanticMasterKind.BRAND)
      .slug(entity.getSlug())
      .logoUrl(entity.getLogoUrl())
      .build();
  }

  private SemanticMaster toDomain(IngredientJpaEntity entity) {
    return baseBuilder(entity, SemanticMasterKind.INGREDIENT)
      .normalizedName(entity.getNormalizedName())
      .inciName(entity.getInciName())
      .build();
  }

  private SemanticMaster toDomain(SkinTypeJpaEntity entity) {
    return baseBuilder(entity, SemanticMasterKind.SKIN_TYPE).build();
  }

  private SemanticMaster toDomain(ConcernJpaEntity entity) {
    return baseBuilder(entity, SemanticMasterKind.CONCERN).build();
  }

  private SemanticMaster toDomain(TagJpaEntity entity) {
    return baseBuilder(entity, SemanticMasterKind.TAG).build();
  }

  private SemanticMaster.SemanticMasterBuilder baseBuilder(AbstractSemanticMasterJpaEntity entity,
                                                           SemanticMasterKind kind) {
    return SemanticMaster.builder()
      .id(entity.getId())
      .kind(kind)
      .code(entity.getCode())
      .name(entity.getName())
      .description(entity.getDescription())
      .isActive(entity.isActive())
      .createdAt(entity.getCreatedAt())
      .updatedAt(entity.getUpdatedAt());
  }

  private BrandJpaEntity toBrandEntity(SemanticMaster semanticMaster) {
    BrandJpaEntity entity = new BrandJpaEntity();
    applyCommonFields(entity, semanticMaster);
    entity.setSlug(semanticMaster.getSlug());
    entity.setLogoUrl(semanticMaster.getLogoUrl());
    return entity;
  }

  private IngredientJpaEntity toIngredientEntity(SemanticMaster semanticMaster) {
    IngredientJpaEntity entity = new IngredientJpaEntity();
    applyCommonFields(entity, semanticMaster);
    entity.setNormalizedName(semanticMaster.getNormalizedName());
    entity.setInciName(semanticMaster.getInciName());
    return entity;
  }

  private SkinTypeJpaEntity toSkinTypeEntity(SemanticMaster semanticMaster) {
    SkinTypeJpaEntity entity = new SkinTypeJpaEntity();
    applyCommonFields(entity, semanticMaster);
    return entity;
  }

  private ConcernJpaEntity toConcernEntity(SemanticMaster semanticMaster) {
    ConcernJpaEntity entity = new ConcernJpaEntity();
    applyCommonFields(entity, semanticMaster);
    return entity;
  }

  private TagJpaEntity toTagEntity(SemanticMaster semanticMaster) {
    TagJpaEntity entity = new TagJpaEntity();
    applyCommonFields(entity, semanticMaster);
    return entity;
  }

  private void applyCommonFields(AbstractSemanticMasterJpaEntity entity, SemanticMaster semanticMaster) {
    entity.setId(semanticMaster.getId());
    entity.setCode(semanticMaster.getCode());
    entity.setName(semanticMaster.getName());
    entity.setDescription(semanticMaster.getDescription());
    entity.setActive(semanticMaster.isActive());
    entity.setCreatedAt(semanticMaster.getCreatedAt());
    entity.setUpdatedAt(semanticMaster.getUpdatedAt());
  }
}
