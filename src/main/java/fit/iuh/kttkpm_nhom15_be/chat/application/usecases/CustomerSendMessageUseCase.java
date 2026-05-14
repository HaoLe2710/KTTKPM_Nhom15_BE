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
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerSendMessageUseCase {

    private final ChatRepository chatRepository;
    private final UserFacade userFacade;
    private final ApplicationEventPublisher eventPublisher;
    private final ChatMessagePayloadSupport payloadSupport;

    @Transactional
    public MessageDTO execute(SendMessageCommand command) {
        payloadSupport.validate(command);

        if (!userFacade.isUserActive(command.senderId())) {
            throw new InactiveChatUserException(command.senderId());
        }

        ChatRoom room = resolveCustomerRoom(command);
        ChatMessage savedMessage = chatRepository.saveMessage(payloadSupport.buildMessage(room.getId(), command));
        MessageDTO response = MessageDTO.from(savedMessage);
        eventPublisher.publishEvent(new ChatMessageSentEvent(room.getId(), response));
        return response;
    }

    private ChatRoom resolveCustomerRoom(SendMessageCommand command) {
        if (command.roomId() == null || command.roomId().isBlank()) {
            return chatRepository.findActiveRoomByCustomer(command.senderId())
                    .orElseGet(() -> chatRepository.saveRoom(ChatRoom.builder()
                            .customerId(command.senderId())
                            .isClosed(false)
                            .build()));
        }

        ChatRoom room = chatRepository.findRoomById(command.roomId())
                .orElseThrow(() -> new ChatRoomNotFoundException(command.roomId()));

        if (room.isClosed()) {
            throw new ChatRoomClosedException(command.roomId());
        }

        if (!command.senderId().equals(room.getCustomerId())) {
            throw new UnauthorizedChatAccessException(command.roomId(), command.senderId());
        }

        return room;
    }
}
