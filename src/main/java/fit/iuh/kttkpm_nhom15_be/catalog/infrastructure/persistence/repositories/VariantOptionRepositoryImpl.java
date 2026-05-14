package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.VariantOption;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantOptionRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.VariantOptionJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.mappers.CatalogDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

interface JpaVariantOptionRepository extends JpaRepository<VariantOptionJpaEntity, String> {
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
}
