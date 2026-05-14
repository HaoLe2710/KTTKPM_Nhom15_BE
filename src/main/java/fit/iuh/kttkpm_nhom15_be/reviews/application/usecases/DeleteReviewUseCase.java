package fit.iuh.kttkpm_nhom15_be.reviews.application.usecases;

import fit.iuh.kttkpm_nhom15_be.reviews.application.commands.DeleteReviewCommand;
import fit.iuh.kttkpm_nhom15_be.reviews.application.events.ProductReviewChangedEvent;
import fit.iuh.kttkpm_nhom15_be.reviews.application.events.ReviewDeletedEvent;
import fit.iuh.kttkpm_nhom15_be.reviews.application.results.DeleteReviewResult;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.ReviewNotFoundException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.UnauthorizedReviewAccessException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.models.Review;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.repositories.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeleteReviewUseCase {

    private final ReviewRepository reviewRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public DeleteReviewResult execute(DeleteReviewCommand command) {
        // 1. Find review by ID
        Optional<Review> reviewOpt = reviewRepository.findById(command.getReviewId());
        if (reviewOpt.isEmpty()) {
            throw new ReviewNotFoundException(command.getReviewId());
        }

        Review review = reviewOpt.get();

        // 2. Verify user owns the review (authorization)
        if (!review.getUserId().equals(command.getUserId())) {
            throw new UnauthorizedReviewAccessException(command.getReviewId(), command.getUserId());
        }

        // 3. Delete review from repository
        reviewRepository.deleteById(command.getReviewId());

        // 4. Publish ReviewDeletedEvent for audit/logging/cleanup
        eventPublisher.publishEvent(new ReviewDeletedEvent(this, review));
        eventPublisher.publishEvent(new ProductReviewChangedEvent(
                review.getProductId(),
                "REVIEW_DELETED",
                LocalDateTime.now()
        ));

        // 5. Return success result
        return DeleteReviewResult.success(command.getReviewId());
    }
}
