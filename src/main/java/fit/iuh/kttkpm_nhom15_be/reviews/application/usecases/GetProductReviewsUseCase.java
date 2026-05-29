package fit.iuh.kttkpm_nhom15_be.reviews.application.usecases;

import fit.iuh.kttkpm_nhom15_be.reviews.application.dto.ProductReviewDTO;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.models.Review;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.repositories.ReviewRepository;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetProductReviewsUseCase {

  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<ProductReviewDTO> execute(String productId) {
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("productId không hợp lệ.");
    }

    return reviewRepository.findByProductId(productId).stream()
      .sorted(Comparator.comparing(Review::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
      .map(review -> new ProductReviewDTO(
        review.getId(),
        resolveReviewerName(review.getUserId()),
        review.getRating(),
        review.getContent(),
        review.getCreatedAt()
      ))
      .toList();
  }

  private String resolveReviewerName(String userId) {
    return userRepository.findById(userId)
      .map(User::getFullName)
      .filter(fullName -> fullName != null && !fullName.isBlank())
      .map(String::trim)
      .orElseGet(() -> buildFallbackReviewerName(userId));
  }

  private String buildFallbackReviewerName(String userId) {
    if (userId == null || userId.isBlank()) {
      return "Khách hàng";
    }

    String normalizedUserId = userId.trim();
    if (normalizedUserId.length() <= 6) {
      return "Khách hàng " + normalizedUserId;
    }

    return "Khách hàng " + normalizedUserId.substring(normalizedUserId.length() - 6);
  }
}
