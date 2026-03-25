package fit.iuh.kttkpm_nhom15_be.reviews.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.reviews.infrastructure.persistence.entities.ReviewJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface JpaReviewRepository extends JpaRepository<ReviewJpaEntity, String> {
    Optional<ReviewJpaEntity> findByUserIdAndProductId(String userId, String productId);
    List<ReviewJpaEntity> findByProductId(String productId);
    List<ReviewJpaEntity> findByUserId(String userId);
    List<ReviewJpaEntity> findByOrderId(String orderId);
    void deleteById(String id);
}
