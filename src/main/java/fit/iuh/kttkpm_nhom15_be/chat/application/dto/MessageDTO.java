package fit.iuh.kttkpm_nhom15_be.chat.application.dto;

import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatMessage;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatMessageType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MessageDTO(
        String id,
        String roomId,
        String senderId,
        ChatMessageType type,
        String content,
        String imageUrl,
        String videoUrl,
        String linkUrl,
        String productId,
        String variantId,
        String productName,
        String productImageUrl,
        BigDecimal productPrice,
        LocalDateTime sentAt
) {
    public static MessageDTO from(ChatMessage message) {
        return new MessageDTO(
                message.getId(),
                message.getRoomId(),
                message.getSenderId(),
                message.getType(),
                message.getContent(),
                message.getImageUrl(),
                message.getVideoUrl(),
                message.getLinkUrl(),
                message.getProductId(),
                message.getVariantId(),
                message.getProductName(),
                message.getProductImageUrl(),
                message.getProductPrice(),
                message.getSentAt()
        );
    }
}
