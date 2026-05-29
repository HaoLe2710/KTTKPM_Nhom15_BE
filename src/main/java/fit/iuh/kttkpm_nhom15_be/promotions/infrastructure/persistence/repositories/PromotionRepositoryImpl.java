package fit.iuh.kttkpm_nhom15_be.promotions.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.repositories.PromotionRepository;
import fit.iuh.kttkpm_nhom15_be.promotions.infrastructure.persistence.entities.PromotionJpaEntity;
import fit.iuh.kttkpm_nhom15_be.promotions.infrastructure.persistence.mappers.PromotionDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

interface JpaPromotionRepository extends JpaRepository<PromotionJpaEntity, String> {
    Optional<PromotionJpaEntity> findByCode(String code);
    List<PromotionJpaEntity> findByType(PromotionType type);

    @Query("""
        SELECT p
        FROM PromotionJpaEntity p
        WHERE p.isActive = true
          AND p.startDate <= :at
          AND p.endDate >= :at
          AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit)
        ORDER BY p.updatedAt DESC, p.createdAt DESC
        """)
    List<PromotionJpaEntity> findActive(@Param("at") LocalDateTime at);

    @Query("""
        SELECT p
        FROM PromotionJpaEntity p
        WHERE p.type = :type
          AND p.isActive = true
          AND p.startDate <= :at
          AND p.endDate >= :at
          AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit)
        ORDER BY p.updatedAt DESC, p.createdAt DESC
        """)
    List<PromotionJpaEntity> findActiveByType(@Param("type") PromotionType type, @Param("at") LocalDateTime at);

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
    public List<Promotion> findAll() {
        return jpaPromotionRepository.findAll().stream()
            .map(promotionDataMapper::toDomainModel)
            .toList();
    }

    @Override
    public List<Promotion> findByType(PromotionType type) {
        return jpaPromotionRepository.findByType(type).stream()
            .map(promotionDataMapper::toDomainModel)
            .toList();
    }

    @Override
    public List<Promotion> findActive(LocalDateTime at) {
        return jpaPromotionRepository.findActive(at).stream()
            .map(promotionDataMapper::toDomainModel)
            .toList();
    }

    @Override
    public List<Promotion> findActiveByType(PromotionType type, LocalDateTime at) {
        return jpaPromotionRepository.findActiveByType(type, at).stream()
            .map(promotionDataMapper::toDomainModel)
            .toList();
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
