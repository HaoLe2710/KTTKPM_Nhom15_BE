package fit.iuh.kttkpm_nhom15_be.chat.application.usecases;

import fit.iuh.kttkpm_nhom15_be.chat.application.commands.SendMessageCommand;
import fit.iuh.kttkpm_nhom15_be.chat.application.dto.MessageDTO;
import fit.iuh.kttkpm_nhom15_be.chat.application.events.ChatMessageSentEvent;
import fit.iuh.kttkpm_nhom15_be.chat.application.support.ChatMessagePayloadSupport;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatRoomClosedException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatRoomNotFoundException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.InactiveChatUserException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.UnauthorizedChatAccessException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatMessage;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatRoom;
import fit.iuh.kttkpm_nhom15_be.chat.domain.repositories.ChatRepository;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StaffReplyMessageUseCase {

    private final ChatRepository chatRepository;
    private final UserFacade userFacade;
    private final ApplicationEventPublisher eventPublisher;
    private final ChatMessagePayloadSupport payloadSupport;

    @Transactional
    public MessageDTO execute(SendMessageCommand command) {
        payloadSupport.validate(command);

        var staff = userFacade.findById(command.senderId())
                .orElseThrow(() -> new InactiveChatUserException(command.senderId()));

        if (!staff.isActive()) {
            throw new InactiveChatUserException(command.senderId());
        }

        if (staff.getRole() != UserRole.STAFF && staff.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedChatAccessException(command.roomId(), command.senderId());
        }

        ChatRoom room = chatRepository.findRoomById(command.roomId())
                .orElseThrow(() -> new ChatRoomNotFoundException(command.roomId()));

        if (room.isClosed()) {
            throw new ChatRoomClosedException(command.roomId());
        }

        if (room.getStaffId() == null) {
            room.setStaffId(command.senderId());
            room = chatRepository.saveRoom(room);
        } else if (!command.senderId().equals(room.getStaffId())) {
            throw new UnauthorizedChatAccessException(command.roomId(), command.senderId());
        }

        ChatMessage savedMessage = chatRepository.saveMessage(payloadSupport.buildMessage(room.getId(), command));
        MessageDTO response = MessageDTO.from(savedMessage);
        eventPublisher.publishEvent(new ChatMessageSentEvent(room.getId(), response));
        return response;
    }
}
