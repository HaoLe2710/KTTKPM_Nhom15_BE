package fit.iuh.kttkpm_nhom15_be.orders.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fit.iuh.kttkpm_nhom15_be.orders.application.dto.OrderDetailDTO;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderItem;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.models.Review;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.repositories.ReviewRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GetMyOrderDetailUseCaseTest {

  @Test
  void getMyOrderDetailBuildsReviewStateForCreatedOrderItems() {
    OrderRepository orderRepository = mock(OrderRepository.class);
    ReviewRepository reviewRepository = mock(ReviewRepository.class);
    GetMyOrderDetailUseCase useCase = new GetMyOrderDetailUseCase(orderRepository, reviewRepository);

    Order order = Order.builder()
      .id("order-1")
      .orderNo("ORD-1")
      .userId("user-1")
      .status(OrderStatus.CREATED)
      .paymentMethod(PaymentMethod.COD)
      .paymentStatus(PaymentStatus.UNPAID)
      .subtotalAmount(BigDecimal.valueOf(200000))
      .discountAmount(BigDecimal.ZERO)
      .shippingFee(BigDecimal.valueOf(15000))
      .totalAmount(BigDecimal.valueOf(215000))
      .shipFullName("Nguyen Van A")
      .shipPhone("0901234567")
      .shipAddress("123 Lê Lợi")
      .shipCity("TP. Ho Chi Minh")
      .shipDistrict("Quan 1")
      .shipWard("Ben Nghe")
      .createdAt(LocalDateTime.of(2026, 5, 28, 10, 0))
      .items(List.of(
        OrderItem.builder()
          .id("item-1")
          .productId("product-1")
          .variantId("variant-1")
          .name("Serum")
          .optionsSnapshot(Map.of("size", "30ml"))
          .quantity(1)
          .unitPrice(BigDecimal.valueOf(200000))
          .lineTotal(BigDecimal.valueOf(200000))
          .build()
      ))
      .build();

    when(orderRepository.findByIdAndUserId("order-1", "user-1")).thenReturn(Optional.of(order));
    when(reviewRepository.findByUserId("user-1")).thenReturn(List.of());

    OrderDetailDTO result = useCase.execute("user-1", "order-1");

    assertEquals("order-1", result.orderId());
    assertEquals(1, result.items().size());
    assertTrue(result.items().get(0).canReview());
    assertEquals("Bạn có thể viết đánh giá cho sản phẩm này.", result.items().get(0).reviewMessage());
  }

  @Test
  void getMyOrderDetailMarksAlreadyReviewedItems() {
    OrderRepository orderRepository = mock(OrderRepository.class);
    ReviewRepository reviewRepository = mock(ReviewRepository.class);
    GetMyOrderDetailUseCase useCase = new GetMyOrderDetailUseCase(orderRepository, reviewRepository);

    Order order = Order.builder()
      .id("order-1")
      .userId("user-1")
      .status(OrderStatus.COMPLETED)
      .items(List.of(
        OrderItem.builder()
          .id("item-1")
          .productId("product-1")
          .name("Serum")
          .quantity(1)
          .unitPrice(BigDecimal.TEN)
          .lineTotal(BigDecimal.TEN)
          .build()
      ))
      .build();

    when(orderRepository.findByIdAndUserId("order-1", "user-1")).thenReturn(Optional.of(order));
    when(reviewRepository.findByUserId("user-1")).thenReturn(List.of(
      Review.builder()
        .id("review-1")
        .userId("user-1")
        .productId("product-1")
        .rating(5)
        .content("Rat tot")
        .createdAt(LocalDateTime.of(2026, 5, 28, 12, 0))
        .build()
    ));

    OrderDetailDTO result = useCase.execute("user-1", "order-1");

    assertTrue(result.items().get(0).alreadyReviewed());
    assertEquals(Integer.valueOf(5), result.items().get(0).reviewRating());
  }
}
