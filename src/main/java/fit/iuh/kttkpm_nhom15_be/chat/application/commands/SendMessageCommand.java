package fit.iuh.kttkpm_nhom15_be.chat.application.commands;

public record SendMessageCommand(String roomId, String senderId, String content) {}
