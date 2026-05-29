package fit.iuh.kttkpm_nhom15_be.reviews.application.usecases;

import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.reviews.application.dto.ReviewEligibilityDTO;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetReviewEligibilityUseCase {

  private final ReviewRepository reviewRepository;
  private final OrderRepository orderRepository;

  @Transactional(readOnly = true)
  public ReviewEligibilityDTO execute(String userId, String productId) {
    if (userId == null || userId.isBlank()) {
      throw new IllegalArgumentException("userId không hợp lệ.");
    }
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("productId không hợp lệ.");
    }

    if (reviewRepository.findByUserIdAndProductId(userId, productId).isPresent()) {
      return new ReviewEligibilityDTO(
        false,
        null,
        true,
        "Bạn đã đánh giá sản phẩm này rồi."
      );
    }

    return orderRepository.findLatestReviewableOrderByUserIdAndProductId(userId, productId)
      .map(order -> new ReviewEligibilityDTO(
        true,
        order.getId(),
        false,
        "Bạn có thể viết đánh giá cho sản phẩm này."
      ))
      .orElseGet(() -> new ReviewEligibilityDTO(
        false,
        null,
        false,
        "Chỉ có thể đánh giá khi đơn hàng có sản phẩm này đang ở trạng thái CREATED hoặc COMPLETED."
      ));
  }
}
