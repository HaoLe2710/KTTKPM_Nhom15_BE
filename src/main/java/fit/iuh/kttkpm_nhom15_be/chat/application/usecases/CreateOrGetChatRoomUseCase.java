package fit.iuh.kttkpm_nhom15_be.chat.application.usecases;

import fit.iuh.kttkpm_nhom15_be.chat.application.dto.ChatRoomDTO;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.InactiveChatUserException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatRoom;
import fit.iuh.kttkpm_nhom15_be.chat.domain.repositories.ChatRepository;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateOrGetChatRoomUseCase {

    private final ChatRepository chatRepository;
    private final UserFacade userFacade;

    @Transactional
    public ChatRoomDTO execute(String customerId) {
        if (!userFacade.isUserActive(customerId)) {
            throw new InactiveChatUserException(customerId);
        }

        ChatRoom room = chatRepository.findActiveRoomByCustomer(customerId)
                .orElseGet(() -> chatRepository.saveRoom(ChatRoom.builder()
                        .customerId(customerId)
                        .isClosed(false)
                        .build()));

        return ChatRoomDTO.from(room);
    }
}
