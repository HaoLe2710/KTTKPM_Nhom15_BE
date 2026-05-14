package fit.iuh.kttkpm_nhom15_be.reviews.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.reviews.domain.models.Review;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
    Review save(Review review);
    Optional<Review> findById(String id);
    Optional<Review> findByUserIdAndProductId(String userId, String productId);
    List<Review> findByProductId(String productId);
    List<Review> findByUserId(String userId);
    List<Review> findByOrderId(String orderId);
    void deleteById(String id);
    boolean existsById(String id);
}
