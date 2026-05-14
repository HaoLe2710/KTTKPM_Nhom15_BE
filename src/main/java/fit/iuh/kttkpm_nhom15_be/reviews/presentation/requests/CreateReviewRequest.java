package fit.iuh.kttkpm_nhom15_be.reviews.presentation.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {
    @NotBlank(message = "User ID không được để trống")
    private String userId;

    @NotBlank(message = "Product ID không được để trống")
    private String productId;

    @NotBlank(message = "Order ID không được để trống")
    private String orderId;

    @Min(value = 1, message = "Điểm đánh giá phải từ 1 đến 5")
    @Max(value = 5, message = "Điểm đánh giá phải từ 1 đến 5")
    private int rating;

    private String content;  // Optional
}
