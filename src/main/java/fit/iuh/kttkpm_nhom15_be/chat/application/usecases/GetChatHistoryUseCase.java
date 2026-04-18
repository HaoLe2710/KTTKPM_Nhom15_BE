package fit.iuh.kttkpm_nhom15_be.chat.application.usecases;

import fit.iuh.kttkpm_nhom15_be.chat.application.dto.MessageDTO;
import fit.iuh.kttkpm_nhom15_be.chat.domain.repositories.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetChatHistoryUseCase {
    private final ChatRepository chatRepository;

    @Transactional(readOnly = true)
    public List<MessageDTO> execute(String roomId) {
        return chatRepository.findMessagesByRoomId(roomId).stream()
                .map(MessageDTO::from)
                .toList();
    }
}
