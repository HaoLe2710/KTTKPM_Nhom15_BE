package fit.iuh.kttkpm_nhom15_be.reviews.application.usecases;

import fit.iuh.kttkpm_nhom15_be.reviews.application.commands.CreateReviewCommand;
import fit.iuh.kttkpm_nhom15_be.reviews.application.events.ProductReviewChangedEvent;
import fit.iuh.kttkpm_nhom15_be.reviews.application.events.ReviewCreatedEvent;
import fit.iuh.kttkpm_nhom15_be.reviews.application.interfaces.OrderFacade;
import fit.iuh.kttkpm_nhom15_be.reviews.application.results.CreateReviewResult;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.InvalidRatingException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.ReviewAlreadyExistsException;
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
public class CreateReviewUseCase {

    private final ReviewRepository reviewRepository;
    private final OrderFacade orderFacade;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreateReviewResult execute(CreateReviewCommand command) {
        // 1. Validate rating (1-5)
        if (command.getRating() < 1 || command.getRating() > 5) {
            throw new InvalidRatingException(command.getRating());
        }

        // 2. Verify order is valid and completed (user owns order, order is COMPLETED)
        orderFacade.verifyOrderForReview(command.getOrderId(), command.getUserId());

        // 3. Check if review already exists for this product (unique constraint)
        Optional<Review> existingReview = reviewRepository.findByUserIdAndProductId(
                command.getUserId(),
                command.getProductId()
        );
        if (existingReview.isPresent()) {
            throw new ReviewAlreadyExistsException(command.getUserId(), command.getProductId());
        }

        // 4. Create review domain model
        Review review = Review.builder()
                .userId(command.getUserId())
                .productId(command.getProductId())
                .orderId(command.getOrderId())
                .rating(command.getRating())
                .content(command.getContent())
                .build();

        // 5. Save review
        Review savedReview = reviewRepository.save(review);

        // 6. Publish event
        eventPublisher.publishEvent(new ReviewCreatedEvent(this, savedReview));
        eventPublisher.publishEvent(new ProductReviewChangedEvent(
                savedReview.getProductId(),
                "REVIEW_CREATED",
                LocalDateTime.now()
        ));

        // 7. Return result
        return CreateReviewResult.builder()
                .id(savedReview.getId())
                .userId(savedReview.getUserId())
                .productId(savedReview.getProductId())
                .orderId(savedReview.getOrderId())
                .rating(savedReview.getRating())
                .content(savedReview.getContent())
                .createdAt(savedReview.getCreatedAt())
                .build();
    }
}
