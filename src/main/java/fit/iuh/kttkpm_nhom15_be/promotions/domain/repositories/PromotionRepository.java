package fit.iuh.kttkpm_nhom15_be.promotions.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository {
    Promotion save(Promotion promotion);
    Optional<Promotion> findById(String id);
    Optional<Promotion> findByCode(String code);
    List<Promotion> findAll();
    List<Promotion> findByType(PromotionType type);
    List<Promotion> findActive(LocalDateTime at);
    List<Promotion> findActiveByType(PromotionType type, LocalDateTime at);
    void deleteById(String id);
    void incrementUsedCount(String id);
}
