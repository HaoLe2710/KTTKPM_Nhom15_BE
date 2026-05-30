package fit.iuh.kttkpm_nhom15_be.chat.application.usecases;

import fit.iuh.kttkpm_nhom15_be.chat.application.dto.ChatRoomDTO;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatRoomClosedException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatRoomNotFoundException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.InactiveChatUserException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.UnauthorizedChatAccessException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatRoom;
import fit.iuh.kttkpm_nhom15_be.chat.domain.repositories.ChatRepository;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignChatRoomUseCase {

    private final ChatRepository chatRepository;
    private final UserFacade userFacade;

    @Transactional
    public ChatRoomDTO execute(String roomId, String staffId) {
        var staff = userFacade.findById(staffId)
                .orElseThrow(() -> new InactiveChatUserException(staffId));

        if (!staff.isActive()) {
            throw new InactiveChatUserException(staffId);
        }

        if (staff.getRole() != UserRole.STAFF && staff.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedChatAccessException(roomId, staffId);
        }

        ChatRoom room = chatRepository.findRoomById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(roomId));

        if (room.isClosed()) {
            throw new ChatRoomClosedException(roomId);
        }

        if (room.getStaffId() != null && !room.getStaffId().equals(staffId)) {
            throw new UnauthorizedChatAccessException(roomId, staffId);
        }

        room.setStaffId(staffId);
        return ChatRoomDTO.from(chatRepository.saveRoom(room));
    }
}
