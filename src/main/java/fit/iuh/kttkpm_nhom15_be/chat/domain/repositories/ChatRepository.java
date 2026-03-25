package fit.iuh.kttkpm_nhom15_be.chat.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatMessage;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatRoom;

import java.util.List;
import java.util.Optional;

public interface ChatRepository {
    // Room
    ChatRoom saveRoom(ChatRoom room);
    Optional<ChatRoom> findRoomById(String roomId);
    List<ChatRoom> findActiveRooms(); // Lấy các phòng chưa đóng để Staff vào chat
    Optional<ChatRoom> findActiveRoomByCustomer(String customerId);

    // Message
    ChatMessage saveMessage(ChatMessage message);
    List<ChatMessage> findMessagesByRoomId(String roomId);
}