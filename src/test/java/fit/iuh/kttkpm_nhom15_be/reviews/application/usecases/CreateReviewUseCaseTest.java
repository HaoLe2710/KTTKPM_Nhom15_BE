package fit.iuh.kttkpm_nhom15_be.reviews.application.usecases;

import fit.iuh.kttkpm_nhom15_be.reviews.application.commands.CreateReviewCommand;
import fit.iuh.kttkpm_nhom15_be.reviews.application.events.ReviewCreatedEvent;
import fit.iuh.kttkpm_nhom15_be.reviews.application.interfaces.OrderFacade;
import fit.iuh.kttkpm_nhom15_be.reviews.application.results.CreateReviewResult;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.InvalidRatingException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.ReviewAlreadyExistsException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.models.Review;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.repositories.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateReviewUseCaseTest {

    @Test
    void executeCreatesReviewSuccessfullyWhenAllValidationsPass() {
        ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
        OrderFacade orderFacade = Mockito.mock(OrderFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        CreateReviewUseCase useCase = new CreateReviewUseCase(reviewRepository, orderFacade, eventPublisher);

        Review savedReview = Review.builder()
                .id("review-1")
                .userId("user-1")
                .productId("product-1")
                .orderId("order-1")
                .rating(5)
                .content("Excellent product!")
                .build();

        doNothing().when(orderFacade).verifyOrderForReview("order-1", "user-1");
        when(reviewRepository.findByUserIdAndProductId("user-1", "product-1")).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        CreateReviewResult result = useCase.execute(new CreateReviewCommand(
                "user-1", "product-1", "order-1", 5, "Excellent product!"
        ));

        ArgumentCaptor<Review> savedReviewCaptor = ArgumentCaptor.forClass(Review.class);
        ArgumentCaptor<ReviewCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ReviewCreatedEvent.class);

        verify(orderFacade).verifyOrderForReview("order-1", "user-1");
        verify(reviewRepository).findByUserIdAndProductId("user-1", "product-1");
        verify(reviewRepository).save(savedReviewCaptor.capture());
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        assertEquals("review-1", result.getId());
        assertEquals("user-1", result.getUserId());
        assertEquals("product-1", result.getProductId());
        assertEquals("order-1", result.getOrderId());
        assertEquals(5, result.getRating());
        assertEquals("Excellent product!", result.getContent());

        assertEquals("review-1", eventCaptor.getValue().getReview().getId());
        assertEquals("user-1", eventCaptor.getValue().getReview().getUserId());
    }

    @Test
    void executeThrowsExceptionWhenRatingIsLessThanOne() {
        ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
        OrderFacade orderFacade = Mockito.mock(OrderFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        CreateReviewUseCase useCase = new CreateReviewUseCase(reviewRepository, orderFacade, eventPublisher);

        InvalidRatingException ex = assertThrows(InvalidRatingException.class, () -> useCase.execute(new CreateReviewCommand(
                "user-1", "product-1", "order-1", 0, "Bad rating"
        )));
        assertTrue(ex.getMessage().contains("Điểm đánh giá phải từ 1 đến 5"));
        assertTrue(ex.getMessage().contains("0"));

        verify(reviewRepository, never()).save(any(Review.class));
        verify(orderFacade, never()).verifyOrderForReview(any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void executeThrowsExceptionWhenRatingIsGreaterThanFive() {
        ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
        OrderFacade orderFacade = Mockito.mock(OrderFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        CreateReviewUseCase useCase = new CreateReviewUseCase(reviewRepository, orderFacade, eventPublisher);

        InvalidRatingException ex = assertThrows(InvalidRatingException.class, () -> useCase.execute(new CreateReviewCommand(
                "user-1", "product-1", "order-1", 6, "Too high"
        )));
        assertTrue(ex.getMessage().contains("Điểm đánh giá phải từ 1 đến 5"));
        assertTrue(ex.getMessage().contains("6"));

        verify(reviewRepository, never()).save(any(Review.class));
        verify(orderFacade, never()).verifyOrderForReview(any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void executeThrowsExceptionWhenReviewAlreadyExistsForUserAndProduct() {
        ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
        OrderFacade orderFacade = Mockito.mock(OrderFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        CreateReviewUseCase useCase = new CreateReviewUseCase(reviewRepository, orderFacade, eventPublisher);

        Review existingReview = Review.builder()
                .id("review-old")
                .userId("user-1")
                .productId("product-1")
                .rating(3)
                .build();

        doNothing().when(orderFacade).verifyOrderForReview("order-1", "user-1");
        when(reviewRepository.findByUserIdAndProductId("user-1", "product-1")).thenReturn(Optional.of(existingReview));

        ReviewAlreadyExistsException ex = assertThrows(ReviewAlreadyExistsException.class, () -> useCase.execute(new CreateReviewCommand(
                "user-1", "product-1", "order-1", 5, "New review"
        )));
        assertTrue(ex.getMessage().contains("Bạn đã đánh giá sản phẩm này rồi"));
        assertTrue(ex.getMessage().contains("user-1"));
        assertTrue(ex.getMessage().contains("product-1"));

        verify(reviewRepository, never()).save(any(Review.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void executeAcceptsAllValidRatingsFromOneToFive() {
        for (int rating = 1; rating <= 5; rating++) {
            ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
            OrderFacade orderFacade = Mockito.mock(OrderFacade.class);
            ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
            CreateReviewUseCase useCase = new CreateReviewUseCase(reviewRepository, orderFacade, eventPublisher);

            Review savedReview = Review.builder()
                    .id("review-" + rating)
                    .userId("user-1")
                    .productId("product-1")
                    .orderId("order-1")
                    .rating(rating)
                    .content("Rating " + rating)
                    .build();

            doNothing().when(orderFacade).verifyOrderForReview("order-1", "user-1");
            when(reviewRepository.findByUserIdAndProductId("user-1", "product-1")).thenReturn(Optional.empty());
            when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

            CreateReviewResult result = useCase.execute(new CreateReviewCommand(
                    "user-1", "product-1", "order-1", rating, "Rating " + rating
            ));

            assertEquals(rating, result.getRating());
            verify(reviewRepository).save(any(Review.class));
        }
    }

    @Test
    void executeAllowsEmptyContentForReview() {
        ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
        OrderFacade orderFacade = Mockito.mock(OrderFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        CreateReviewUseCase useCase = new CreateReviewUseCase(reviewRepository, orderFacade, eventPublisher);

        Review savedReview = Review.builder()
                .id("review-1")
                .userId("user-1")
                .productId("product-1")
                .orderId("order-1")
                .rating(5)
                .content(null)
                .build();

        doNothing().when(orderFacade).verifyOrderForReview("order-1", "user-1");
        when(reviewRepository.findByUserIdAndProductId("user-1", "product-1")).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        CreateReviewResult result = useCase.execute(new CreateReviewCommand(
                "user-1", "product-1", "order-1", 5, null
        ));

        assertEquals("review-1", result.getId());
        assertEquals(null, result.getContent());
        verify(reviewRepository).save(any(Review.class));
        verify(eventPublisher).publishEvent(any());
    }
}
