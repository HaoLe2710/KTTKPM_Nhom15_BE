package fit.iuh.kttkpm_nhom15_be.reviews.application.usecases;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.reviews.application.dto.ReviewEligibilityDTO;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.models.Review;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.repositories.ReviewRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetReviewEligibilityUseCaseTest {

    @Test
    void executeReturnsAlreadyReviewedWhenExistingReviewIsFound() {
        ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        GetReviewEligibilityUseCase useCase = new GetReviewEligibilityUseCase(reviewRepository, orderRepository);

        when(reviewRepository.findByUserIdAndProductId("user-1", "product-1"))
                .thenReturn(Optional.of(Review.builder().id("review-1").build()));

        ReviewEligibilityDTO result = useCase.execute("user-1", "product-1");

        assertFalse(result.canReview());
        assertTrue(result.alreadyReviewed());
        assertEquals(null, result.orderId());
        verify(reviewRepository).findByUserIdAndProductId("user-1", "product-1");
    }

    @Test
    void executeReturnsEligibleOrderWhenReviewablePurchaseExists() {
        ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        GetReviewEligibilityUseCase useCase = new GetReviewEligibilityUseCase(reviewRepository, orderRepository);

        when(reviewRepository.findByUserIdAndProductId("user-1", "product-1")).thenReturn(Optional.empty());
        when(orderRepository.findLatestReviewableOrderByUserIdAndProductId("user-1", "product-1"))
                .thenReturn(Optional.of(Order.builder().id("order-123").build()));

        ReviewEligibilityDTO result = useCase.execute("user-1", "product-1");

        assertTrue(result.canReview());
        assertFalse(result.alreadyReviewed());
        assertEquals("order-123", result.orderId());
    }

    @Test
    void executeReturnsIneligibleWhenUserHasNoReviewableOrderForProduct() {
        ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        GetReviewEligibilityUseCase useCase = new GetReviewEligibilityUseCase(reviewRepository, orderRepository);

        when(reviewRepository.findByUserIdAndProductId("user-1", "product-1")).thenReturn(Optional.empty());
        when(orderRepository.findLatestReviewableOrderByUserIdAndProductId("user-1", "product-1"))
                .thenReturn(Optional.empty());

        ReviewEligibilityDTO result = useCase.execute("user-1", "product-1");

        assertFalse(result.canReview());
        assertFalse(result.alreadyReviewed());
        assertEquals(null, result.orderId());
    }
}
