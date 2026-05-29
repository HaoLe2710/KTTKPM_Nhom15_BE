package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.VariantOption;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantOptionRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.VariantOptionJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.mappers.CatalogDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

interface JpaVariantOptionRepository extends JpaRepository<VariantOptionJpaEntity, String> {
    @Query("SELECT COUNT(vo) > 0 FROM VariantOptionJpaEntity vo WHERE vo.optionValue.id IN :ids")
    boolean existsByOptionValueIds(@Param("ids") List<String> optionValueIds);
}

@Repository
@RequiredArgsConstructor
public class VariantOptionRepositoryImpl implements VariantOptionRepository {

    private final JpaVariantOptionRepository jpaRepository;
    private final CatalogDataMapper dataMapper;

    @Override
    public VariantOption save(VariantOption variantOption) {
        VariantOptionJpaEntity entity = dataMapper.toJpaEntity(variantOption);
        return dataMapper.toDomainModel(jpaRepository.save(entity));
    }

    @Override
    public boolean existsByOptionValueIds(List<String> optionValueIds) {
        if (optionValueIds == null || optionValueIds.isEmpty()) {
            return false;
        }
        return jpaRepository.existsByOptionValueIds(optionValueIds);
    }
}
