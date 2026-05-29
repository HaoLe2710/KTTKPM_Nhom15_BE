package fit.iuh.kttkpm_nhom15_be.reviews.application.usecases;

import fit.iuh.kttkpm_nhom15_be.reviews.application.dto.ProductReviewDTO;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.models.Review;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.repositories.ReviewRepository;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetProductReviewsUseCaseTest {

    @Test
    void executeReturnsReviewsSortedNewestFirstWithReviewerNames() {
        ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        GetProductReviewsUseCase useCase = new GetProductReviewsUseCase(reviewRepository, userRepository);

        Review olderReview = Review.builder()
                .id("review-1")
                .userId("user-1")
                .productId("product-1")
                .rating(4)
                .content("Older review")
                .createdAt(LocalDateTime.of(2026, 5, 1, 10, 0))
                .build();
        Review newerReview = Review.builder()
                .id("review-2")
                .userId("user-2")
                .productId("product-1")
                .rating(5)
                .content("Newer review")
                .createdAt(LocalDateTime.of(2026, 5, 2, 12, 30))
                .build();

        when(reviewRepository.findByProductId("product-1")).thenReturn(List.of(olderReview, newerReview));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(User.builder().id("user-1").fullName("Lan").build()));
        when(userRepository.findById("user-2")).thenReturn(Optional.of(User.builder().id("user-2").fullName("Minh").build()));

        List<ProductReviewDTO> result = useCase.execute("product-1");

        assertEquals(2, result.size());
        assertEquals("review-2", result.get(0).id());
        assertEquals("Minh", result.get(0).reviewerName());
        assertEquals("review-1", result.get(1).id());
        assertEquals("Lan", result.get(1).reviewerName());
        verify(reviewRepository).findByProductId("product-1");
    }

    @Test
    void executeFallsBackToMaskedReviewerNameWhenUserProfileIsMissing() {
        ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        GetProductReviewsUseCase useCase = new GetProductReviewsUseCase(reviewRepository, userRepository);

        Review review = Review.builder()
                .id("review-3")
                .userId("customer-123456")
                .productId("product-1")
                .rating(3)
                .content("Fallback reviewer")
                .createdAt(LocalDateTime.of(2026, 5, 3, 8, 15))
                .build();

        when(reviewRepository.findByProductId("product-1")).thenReturn(List.of(review));
        when(userRepository.findById("customer-123456")).thenReturn(Optional.empty());

        List<ProductReviewDTO> result = useCase.execute("product-1");

        assertEquals(1, result.size());
        assertEquals("Khách hàng 123456", result.get(0).reviewerName());
    }
}
