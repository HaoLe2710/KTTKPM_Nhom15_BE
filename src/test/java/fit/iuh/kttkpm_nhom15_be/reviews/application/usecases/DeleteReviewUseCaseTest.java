package fit.iuh.kttkpm_nhom15_be.reviews.application.usecases;

import fit.iuh.kttkpm_nhom15_be.reviews.application.commands.DeleteReviewCommand;
import fit.iuh.kttkpm_nhom15_be.reviews.application.events.ProductReviewChangedEvent;
import fit.iuh.kttkpm_nhom15_be.reviews.application.events.ReviewDeletedEvent;
import fit.iuh.kttkpm_nhom15_be.reviews.application.results.DeleteReviewResult;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.ReviewNotFoundException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.UnauthorizedReviewAccessException;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteReviewUseCaseTest {

    @Test
    void executeDeletesReviewSuccessfullyWhenUserOwnsIt() {
        ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        DeleteReviewUseCase useCase = new DeleteReviewUseCase(reviewRepository, eventPublisher);

        Review review = deletableReview("review-1", "user-1");

        when(reviewRepository.findById("review-1")).thenReturn(Optional.of(review));

        DeleteReviewResult result = useCase.execute(new DeleteReviewCommand("review-1", "user-1"));

        ArgumentCaptor<ReviewDeletedEvent> eventCaptor = ArgumentCaptor.forClass(ReviewDeletedEvent.class);

        verify(reviewRepository).deleteById("review-1");
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        verify(eventPublisher).publishEvent(any(ProductReviewChangedEvent.class));

        assertEquals("review-1", result.getReviewId());
        assertEquals("Đánh giá đã được xóa thành công", result.getMessage());
        assertEquals("review-1", eventCaptor.getValue().getReview().getId());
        assertEquals("user-1", eventCaptor.getValue().getReview().getUserId());
    }

    @Test
    void executeThrowsExceptionWhenReviewDoesNotExist() {
        ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        DeleteReviewUseCase useCase = new DeleteReviewUseCase(reviewRepository, eventPublisher);

        when(reviewRepository.findById("missing")).thenReturn(Optional.empty());

        ReviewNotFoundException ex = assertThrows(ReviewNotFoundException.class, () -> useCase.execute(new DeleteReviewCommand("missing", "user-1")));
        assertTrue(ex.getMessage().contains("Đánh giá không tồn tại"));
        assertTrue(ex.getMessage().contains("missing"));

        verify(reviewRepository, never()).deleteById(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void executeThrowsExceptionWhenUserDoesNotOwnReview() {
        ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        DeleteReviewUseCase useCase = new DeleteReviewUseCase(reviewRepository, eventPublisher);

        Review review = deletableReview("review-1", "user-1");

        when(reviewRepository.findById("review-1")).thenReturn(Optional.of(review));

        UnauthorizedReviewAccessException ex = assertThrows(UnauthorizedReviewAccessException.class, 
            () -> useCase.execute(new DeleteReviewCommand("review-1", "different-user")));
        assertTrue(ex.getMessage().contains("Bạn không có quyền xóa đánh giá này"));
        assertTrue(ex.getMessage().contains("review-1"));
        assertTrue(ex.getMessage().contains("different-user"));

        verify(reviewRepository, never()).deleteById(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void executePublishesEventWithDeletedReviewData() {
        ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        DeleteReviewUseCase useCase = new DeleteReviewUseCase(reviewRepository, eventPublisher);

        Review review = Review.builder()
                .id("review-123")
                .userId("user-1")
                .productId("product-456")
                .orderId("order-789")
                .rating(4)
                .content("Great product")
                .build();

        when(reviewRepository.findById("review-123")).thenReturn(Optional.of(review));

        useCase.execute(new DeleteReviewCommand("review-123", "user-1"));

        ArgumentCaptor<ReviewDeletedEvent> eventCaptor = ArgumentCaptor.forClass(ReviewDeletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        verify(eventPublisher).publishEvent(any(ProductReviewChangedEvent.class));

        Review deletedReviewData = eventCaptor.getValue().getReview();
        assertEquals("review-123", deletedReviewData.getId());
        assertEquals("user-1", deletedReviewData.getUserId());
        assertEquals("product-456", deletedReviewData.getProductId());
        assertEquals("order-789", deletedReviewData.getOrderId());
        assertEquals(4, deletedReviewData.getRating());
        assertEquals("Great product", deletedReviewData.getContent());
    }

    @Test
    void executeDeletesReviewFromRepositoryBeforePublishingEvent() {
        ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        DeleteReviewUseCase useCase = new DeleteReviewUseCase(reviewRepository, eventPublisher);

        Review review = deletableReview("review-1", "user-1");

        when(reviewRepository.findById("review-1")).thenReturn(Optional.of(review));

        useCase.execute(new DeleteReviewCommand("review-1", "user-1"));

        // Verify delete was called
        verify(reviewRepository).deleteById("review-1");
        // Verify event was published
        verify(eventPublisher).publishEvent(any(ReviewDeletedEvent.class));
        verify(eventPublisher).publishEvent(any(ProductReviewChangedEvent.class));
    }

    private Review deletableReview(String reviewId, String userId) {
        return Review.builder()
                .id(reviewId)
                .userId(userId)
                .productId("product-1")
                .orderId("order-1")
                .rating(5)
                .content("Good product")
                .build();
    }
}
