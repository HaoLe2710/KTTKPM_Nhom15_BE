package fit.iuh.kttkpm_nhom15_be.reviews.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.reviews.application.commands.CreateReviewCommand;
import fit.iuh.kttkpm_nhom15_be.reviews.application.commands.DeleteReviewCommand;
import fit.iuh.kttkpm_nhom15_be.reviews.application.results.CreateReviewResult;
import fit.iuh.kttkpm_nhom15_be.reviews.application.results.DeleteReviewResult;
import fit.iuh.kttkpm_nhom15_be.reviews.application.usecases.CreateReviewUseCase;
import fit.iuh.kttkpm_nhom15_be.reviews.application.usecases.DeleteReviewUseCase;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.InvalidRatingException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.OrderNotCompletedException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.OrderNotFoundException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.ReviewAlreadyExistsException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.ReviewNotFoundException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.UnauthorizedReviewAccessException;
import fit.iuh.kttkpm_nhom15_be.reviews.presentation.requests.CreateReviewRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final CreateReviewUseCase createReviewUseCase;
    private final DeleteReviewUseCase deleteReviewUseCase;

    /**
     * POST /api/v1/reviews
     * Tạo đánh giá sản phẩm: xác thực đơn hàng, kiểm tra trùng lặp, lưu đánh giá, publish sự kiện.
     */
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

    /**
     * DELETE /api/v1/reviews/{reviewId}
     * Xóa đánh giá: xác thực quyền sở hữu, xóa, publish sự kiện.
     */
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

    // --- Exception Handlers ---

    @ExceptionHandler(InvalidRatingException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRating(InvalidRatingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of("error", ex.getMessage())
        );
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleOrderNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("error", ex.getMessage())
        );
    }

    @ExceptionHandler(OrderNotCompletedException.class)
    public ResponseEntity<Map<String, String>> handleOrderNotCompleted(OrderNotCompletedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of("error", ex.getMessage())
        );
    }

    @ExceptionHandler(ReviewAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleReviewAlreadyExists(ReviewAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                Map.of("error", ex.getMessage())
        );
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleReviewNotFound(ReviewNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of("error", ex.getMessage())
        );
    }

    @ExceptionHandler(UnauthorizedReviewAccessException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedReviewAccess(UnauthorizedReviewAccessException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                Map.of("error", ex.getMessage())
        );
    }
}
