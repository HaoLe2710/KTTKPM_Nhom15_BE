package fit.iuh.kttkpm_nhom15_be.chat.domain.models;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatMessage {
    private String id;
    private String roomId;
    private String senderId;
    private ChatMessageType type;
    private String content;
    private String imageUrl;
    private String linkUrl;
    private String productId;
    private String variantId;
    private String productName;
    private String productImageUrl;
    private BigDecimal productPrice;
    private LocalDateTime sentAt;
}
