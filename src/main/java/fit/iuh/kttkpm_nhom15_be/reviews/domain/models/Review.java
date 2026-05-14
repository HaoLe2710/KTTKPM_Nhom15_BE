package fit.iuh.kttkpm_nhom15_be.reviews.domain.models;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Review {
    private String id;
    private String userId;
    private String productId;
    private String orderId;
    private int rating;
    private String content;
    private LocalDateTime createdAt;
}