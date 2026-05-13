package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Media;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.MediaRepository;
import jakarta.transaction.Transactional;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.MediaJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.ProductJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.entites.VariantJpaEntity;
import fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.mappers.CatalogDataMapper;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

interface JpaMediaRepository extends JpaRepository<MediaJpaEntity, String> {
    List<MediaJpaEntity> findByProduct_IdAndVariantIsNullOrderByCreatedAtAsc(String productId);
    List<MediaJpaEntity> findByVariant_IdOrderByCreatedAtAsc(String variantId);

    @Modifying
    @Transactional
    @Query("update MediaJpaEntity m set m.isPrimary = false where m.product.id = :productId and m.variant is null")
    void clearPrimaryForProduct(@Param("productId") String productId);

    @Modifying
    @Transactional
    @Query("update MediaJpaEntity m set m.isPrimary = false where m.variant.id = :variantId")
    void clearPrimaryForVariant(@Param("variantId") String variantId);
}

@Repository
@RequiredArgsConstructor
public class MediaRepositoryImpl implements MediaRepository {

    private final JpaMediaRepository jpaRepository;
    private final CatalogDataMapper dataMapper;
    private final EntityManager entityManager;

    @Override
    public Media save(Media media) {
        MediaJpaEntity entity = dataMapper.toJpaEntity(media);
        if (media.getProductId() != null && !media.getProductId().isBlank()) {
            entity.setProduct(entityManager.getReference(ProductJpaEntity.class, media.getProductId()));
        }
        if (media.getVariantId() != null && !media.getVariantId().isBlank()) {
            entity.setVariant(entityManager.getReference(VariantJpaEntity.class, media.getVariantId()));
        } else {
            entity.setVariant(null);
        }
        return dataMapper.toDomainModel(jpaRepository.save(entity));
    }

    @Override
    public Optional<Media> findById(String id) {
        return jpaRepository.findById(id).map(dataMapper::toDomainModel);
    }

    @Override
    public List<Media> findProductMedia(String productId) {
        return jpaRepository.findByProduct_IdAndVariantIsNullOrderByCreatedAtAsc(productId).stream()
                .map(dataMapper::toDomainModel)
                .toList();
    }

    @Override
    public List<Media> findVariantMedia(String variantId) {
        return jpaRepository.findByVariant_IdOrderByCreatedAtAsc(variantId).stream()
                .map(dataMapper::toDomainModel)
                .toList();
    }

    @Override
    public void clearPrimaryForProduct(String productId) {
        jpaRepository.clearPrimaryForProduct(productId);
    }

    @Override
    public void clearPrimaryForVariant(String variantId) {
        jpaRepository.clearPrimaryForVariant(variantId);
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }
}
