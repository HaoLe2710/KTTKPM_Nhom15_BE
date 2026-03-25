package fit.iuh.kttkpm_nhom15_be.chat.application.events;

public record ChatMessageSentEvent(String roomId, String messageId, String senderId, String content) {}
