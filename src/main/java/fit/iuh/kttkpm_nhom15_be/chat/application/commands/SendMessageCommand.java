package fit.iuh.kttkpm_nhom15_be.chat.application.commands;

import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatMessageType;

import java.math.BigDecimal;

public record SendMessageCommand(
        String roomId,
        String senderId,
        ChatMessageType type,
        String content,
        String imageUrl,
        String linkUrl,
        String productId,
        String variantId,
        String productName,
        String productImageUrl,
        BigDecimal productPrice
) {}
