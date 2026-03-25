package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.ProductType;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductTypeRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.ProductTypeJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.mappers.CatalogDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

interface JpaProductTypeRepository extends JpaRepository<ProductTypeJpaEntity, String> {
}

@Repository
@RequiredArgsConstructor
public class ProductTypeRepositoryImpl implements ProductTypeRepository {

    private final JpaProductTypeRepository jpaRepository;
    private final CatalogDataMapper dataMapper;

    @Override
    public ProductType save(ProductType productType) {
        ProductTypeJpaEntity entity = dataMapper.toJpaEntity(productType);
        return dataMapper.toDomainModel(jpaRepository.save(entity));
    }

    @Override
    public Optional<ProductType> findById(String id) {
        return jpaRepository.findById(id).map(dataMapper::toDomainModel);
    }

    @Override
    public List<ProductType> findAll() {
        return jpaRepository.findAll().stream()
                .map(dataMapper::toDomainModel)
                .collect(Collectors.toList());
    }
}
