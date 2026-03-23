package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Variant;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.VariantJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.mappers.CatalogDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

// 1. Interface Spring Data JPA thông thường
interface JpaVariantRepository extends JpaRepository<VariantJpaEntity, String> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE VariantJpaEntity v SET v.stockQuantity = v.stockQuantity + :addedStock, v.price = :price WHERE v.id = :id")
    int applyPriceAndStockPatch(@Param("id") String id, @Param("price") BigDecimal price, @Param("addedStock") int addedStock);
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

  @Override
  public Variant save(Variant variant) {
      VariantJpaEntity entity = catalogDataMapper.toJpaEntity(variant);
      return catalogDataMapper.toDomainModel(jpaVariantRepository.save(entity));
  }

  @Override
  public void patchPriceAndStock(String id, java.math.BigDecimal price, int addedStock) {
      jpaVariantRepository.applyPriceAndStockPatch(id, price, addedStock);
  }
}
