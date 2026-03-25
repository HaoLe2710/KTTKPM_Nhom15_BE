package fit.iuh.kttkpm_nhom15_be.reviews.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteReviewCommand {
    private String reviewId;
    private String userId;  // To verify ownership
}
