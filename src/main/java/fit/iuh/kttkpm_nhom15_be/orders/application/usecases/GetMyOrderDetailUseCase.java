package fit.iuh.kttkpm_nhom15_be.orders.application.usecases;

import fit.iuh.kttkpm_nhom15_be.orders.application.dto.OrderDetailDTO;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.OrderDetailItemDTO;
import fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions.OrderNotFoundException;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderItem;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.models.Review;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.repositories.ReviewRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetMyOrderDetailUseCase {

  private final OrderRepository orderRepository;
  private final ReviewRepository reviewRepository;

  @Transactional(readOnly = true)
  public OrderDetailDTO execute(String userId, String orderId) {
    Order order = orderRepository.findByIdAndUserId(orderId, userId)
      .orElseThrow(() -> new OrderNotFoundException(orderId));

    Map<String, Review> reviewsByProductId = reviewRepository.findByUserId(userId).stream()
      .filter(review -> review.getProductId() != null && !review.getProductId().isBlank())
      .collect(Collectors.toMap(Review::getProductId, Function.identity(), (left, right) -> left));

    List<OrderDetailItemDTO> items = order.getItems().stream()
      .map(item -> mapItem(order, item, reviewsByProductId.get(item.getProductId())))
      .toList();

    return new OrderDetailDTO(
      order.getId(),
      order.getOrderNo(),
      order.getStatus(),
      order.getPaymentMethod(),
      order.getPaymentStatus(),
      order.getSubtotalAmount(),
      order.getDiscountAmount(),
      order.getShippingFee(),
      order.getTotalAmount(),
      order.getShipFullName(),
      order.getShipPhone(),
      order.getShipAddress(),
      order.getShipCity(),
      order.getShipDistrict(),
      order.getShipWard(),
      order.getCreatedAt(),
      items
    );
  }

  private OrderDetailItemDTO mapItem(Order order, OrderItem item, Review review) {
    boolean reviewableOrder = isReviewableStatus(order.getStatus());
    boolean alreadyReviewed = review != null;
    boolean canReview = reviewableOrder && !alreadyReviewed && item.getProductId() != null && !item.getProductId().isBlank();

    String reviewMessage = reviewableOrder
      ? alreadyReviewed
        ? "Bạn đã đánh giá sản phẩm này rồi."
        : canReview
          ? "Bạn có thể viết đánh giá cho sản phẩm này."
          : "Sản phẩm này hiện chưa sẵn sàng để đánh giá."
      : null;

    return new OrderDetailItemDTO(
      item.getId(),
      item.getProductId(),
      item.getVariantId(),
      item.getSku(),
      item.getName(),
      item.getImageUrl(),
      item.getOptionsSnapshot(),
      item.getQuantity(),
      item.getUnitPrice(),
      item.getLineTotal(),
      canReview,
      alreadyReviewed,
      review != null ? review.getRating() : null,
      review != null ? review.getContent() : null,
      review != null ? review.getCreatedAt() : null,
      reviewMessage
    );
  }

  private boolean isReviewableStatus(OrderStatus status) {
    return status == OrderStatus.CREATED || status == OrderStatus.COMPLETED;
  }
}
