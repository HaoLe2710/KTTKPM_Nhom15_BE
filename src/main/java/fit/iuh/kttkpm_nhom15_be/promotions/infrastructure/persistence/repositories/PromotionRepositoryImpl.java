package fit.iuh.kttkpm_nhom15_be.promotions.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.repositories.PromotionRepository;
import fit.iuh.kttkpm_nhom15_be.promotions.infrastructure.persistence.entities.PromotionJpaEntity;
import fit.iuh.kttkpm_nhom15_be.promotions.infrastructure.persistence.mappers.PromotionDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

interface JpaPromotionRepository extends JpaRepository<PromotionJpaEntity, String> {
    Optional<PromotionJpaEntity> findByCode(String code);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PromotionJpaEntity p SET p.usedCount = p.usedCount + 1 WHERE p.id = :id")
    int incrementUsedCount(@Param("id") String id);
}

@Repository
@RequiredArgsConstructor
public class PromotionRepositoryImpl implements PromotionRepository {
    private final JpaPromotionRepository jpaPromotionRepository;
    private final PromotionDataMapper promotionDataMapper;

    @Override
    public Promotion save(Promotion promotion) {
        PromotionJpaEntity entity = promotionDataMapper.toJpaEntity(promotion);
        return promotionDataMapper.toDomainModel(jpaPromotionRepository.save(entity));
    }

    @Override
    public Optional<Promotion> findById(String id) {
        return jpaPromotionRepository.findById(id).map(promotionDataMapper::toDomainModel);
    }

    @Override
    public Optional<Promotion> findByCode(String code) {
        return jpaPromotionRepository.findByCode(code).map(promotionDataMapper::toDomainModel);
    }

    @Override
    public void deleteById(String id) {
        jpaPromotionRepository.deleteById(id);
    }

    @Override
    public void incrementUsedCount(String id) {
        jpaPromotionRepository.incrementUsedCount(id);
    }
}
