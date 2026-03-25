package fit.iuh.kttkpm_nhom15_be.reviews.application.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteReviewResult {
    private String reviewId;
    private String message;  // "Đánh giá đã được xóa thành công"

    public static DeleteReviewResult success(String reviewId) {
        return DeleteReviewResult.builder()
                .reviewId(reviewId)
                .message("Đánh giá đã được xóa thành công")
                .build();
    }
}
