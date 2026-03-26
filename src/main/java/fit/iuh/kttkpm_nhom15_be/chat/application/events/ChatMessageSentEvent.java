package fit.iuh.kttkpm_nhom15_be.chat.application.events;

import fit.iuh.kttkpm_nhom15_be.chat.application.dto.MessageDTO;

public record ChatMessageSentEvent(String roomId, MessageDTO message) {}
