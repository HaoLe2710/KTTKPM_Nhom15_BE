package fit.iuh.kttkpm_nhom15_be.promotions.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;

import java.util.Optional;

public interface PromotionRepository {
    Promotion save(Promotion promotion);
    Optional<Promotion> findById(String id);
    Optional<Promotion> findByCode(String code);
    void deleteById(String id);
    void incrementUsedCount(String id);
}
