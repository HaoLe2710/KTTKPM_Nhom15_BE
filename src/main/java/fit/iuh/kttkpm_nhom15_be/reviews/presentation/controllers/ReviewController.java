package fit.iuh.kttkpm_nhom15_be.reviews.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.reviews.application.commands.CreateReviewCommand;
import fit.iuh.kttkpm_nhom15_be.reviews.application.commands.DeleteReviewCommand;
import fit.iuh.kttkpm_nhom15_be.reviews.application.results.CreateReviewResult;
import fit.iuh.kttkpm_nhom15_be.reviews.application.results.DeleteReviewResult;
import fit.iuh.kttkpm_nhom15_be.reviews.application.usecases.CreateReviewUseCase;
import fit.iuh.kttkpm_nhom15_be.reviews.application.usecases.DeleteReviewUseCase;
import fit.iuh.kttkpm_nhom15_be.reviews.presentation.requests.CreateReviewRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final CreateReviewUseCase createReviewUseCase;
    private final DeleteReviewUseCase deleteReviewUseCase;

    @PostMapping
    public ResponseEntity<CreateReviewResult> createReview(@Valid @RequestBody CreateReviewRequest request) {
        CreateReviewCommand command = CreateReviewCommand.builder()
                .userId(request.getUserId())
                .productId(request.getProductId())
                .orderId(request.getOrderId())
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        CreateReviewResult result = createReviewUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<DeleteReviewResult> deleteReview(
            @PathVariable String reviewId,
            @RequestParam String userId
    ) {
        DeleteReviewCommand command = DeleteReviewCommand.builder()
                .reviewId(reviewId)
                .userId(userId)
                .build();

        DeleteReviewResult result = deleteReviewUseCase.execute(command);
        return ResponseEntity.ok(result);
    }
}
