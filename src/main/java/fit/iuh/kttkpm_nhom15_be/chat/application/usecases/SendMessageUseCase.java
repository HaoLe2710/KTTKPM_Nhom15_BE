package fit.iuh.kttkpm_nhom15_be.chat.application.usecases;

import fit.iuh.kttkpm_nhom15_be.chat.application.commands.SendMessageCommand;
import fit.iuh.kttkpm_nhom15_be.chat.application.events.ChatMessageSentEvent;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatMessage;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatRoom;
import fit.iuh.kttkpm_nhom15_be.chat.domain.repositories.ChatRepository;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SendMessageUseCase {

    private final ChatRepository chatRepository;
    private final UserFacade userFacade;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ChatMessage execute(SendMessageCommand command) {
        // 1. Kiểm tra phòng chat có tồn tại và chưa đóng không?
        ChatRoom room = chatRepository.findRoomById(command.roomId())
                .orElseThrow(() -> new RuntimeException("Phòng chat không tồn tại"));
        if (room.isClosed()) {
            throw new RuntimeException("Phòng chat này đã kết thúc.");
        }

        // 2. Ràng buộc chéo (Cross-module): Khách hàng có bị khóa tài khoản không?
        if (!userFacade.isUserActive(room.getCustomerId())) {
            throw new RuntimeException("Không thể chat. Tài khoản khách hàng này đã bị vô hiệu hóa.");
        }

        // 3. Khởi tạo và lưu Message
        ChatMessage message = ChatMessage.builder()
                .roomId(room.getId())
                .senderId(command.senderId())
                .content(command.content())
                .sentAt(LocalDateTime.now())
                .build();

        ChatMessage savedMsg = chatRepository.saveMessage(message);

        // 4. Bắn Event để WebSocket lắng nghe và đẩy tin nhắn Real-time lên UI
        eventPublisher.publishEvent(new ChatMessageSentEvent(
                room.getId(), savedMsg.getId(), savedMsg.getSenderId(), savedMsg.getContent()
        ));

        return savedMsg;
    }
}