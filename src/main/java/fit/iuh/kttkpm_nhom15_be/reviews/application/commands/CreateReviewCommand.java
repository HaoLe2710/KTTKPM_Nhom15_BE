package fit.iuh.kttkpm_nhom15_be.reviews.application.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewCommand {
    private String userId;
    private String productId;
    private String orderId;
    private int rating;
    private String content;
}
