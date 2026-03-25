package fit.iuh.kttkpm_nhom15_be.reviews.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private String id;
    private String userId;
    private String productId;
    private String orderId;
    private int rating;
    private String content;
    private LocalDateTime createdAt;
}
