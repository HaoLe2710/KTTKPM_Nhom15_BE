package fit.iuh.kttkpm_nhom15_be.chat.application.usecases;

import fit.iuh.kttkpm_nhom15_be.chat.application.dto.ChatRoomDTO;
import fit.iuh.kttkpm_nhom15_be.chat.domain.repositories.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetActiveChatRoomsUseCase {

    private final ChatRepository chatRepository;

    @Transactional(readOnly = true)
    public List<ChatRoomDTO> execute() {
        return chatRepository.findActiveRooms().stream()
                .map(ChatRoomDTO::from)
                .toList();
    }
}
