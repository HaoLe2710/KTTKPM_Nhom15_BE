package fit.iuh.kttkpm_nhom15_be.reviews.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.reviews.domain.models.Review;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.repositories.ReviewRepository;
import fit.iuh.kttkpm_nhom15_be.reviews.infrastructure.persistence.entities.ReviewJpaEntity;
import fit.iuh.kttkpm_nhom15_be.reviews.infrastructure.persistence.mappers.ReviewDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final JpaReviewRepository jpaReviewRepository;
    private final ReviewDataMapper reviewDataMapper;

    @Override
    public Review save(Review review) {
        ReviewJpaEntity entity = reviewDataMapper.toJpaEntity(review);
        ReviewJpaEntity savedEntity = jpaReviewRepository.save(entity);
        return reviewDataMapper.toDomainModel(savedEntity);
    }

    @Override
    public Optional<Review> findById(String id) {
        return jpaReviewRepository.findById(id)
                .map(reviewDataMapper::toDomainModel);
    }

    @Override
    public Optional<Review> findByUserIdAndProductId(String userId, String productId) {
        return jpaReviewRepository.findByUserIdAndProductId(userId, productId)
                .map(reviewDataMapper::toDomainModel);
    }

    @Override
    public List<Review> findByProductId(String productId) {
        return jpaReviewRepository.findByProductId(productId)
                .stream()
                .map(reviewDataMapper::toDomainModel)
                .toList();
    }

    @Override
    public List<Review> findByUserId(String userId) {
        return jpaReviewRepository.findByUserId(userId)
                .stream()
                .map(reviewDataMapper::toDomainModel)
                .toList();
    }

    @Override
    public List<Review> findByOrderId(String orderId) {
        return jpaReviewRepository.findByOrderId(orderId)
                .stream()
                .map(reviewDataMapper::toDomainModel)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        jpaReviewRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return jpaReviewRepository.existsById(id);
    }
}
