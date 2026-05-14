package fit.iuh.kttkpm_nhom15_be.chat.application.dto;

import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatRoom;

import java.time.LocalDateTime;

public record ChatRoomDTO(
        String id,
        String customerId,
        String staffId,
        boolean isClosed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ChatRoomDTO from(ChatRoom room) {
        return new ChatRoomDTO(
                room.getId(),
                room.getCustomerId(),
                room.getStaffId(),
                room.isClosed(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }
}
