package fit.iuh.kttkpm_nhom15_be.chat.application.usecases;

import fit.iuh.kttkpm_nhom15_be.chat.application.dto.ChatRoomDTO;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatRoomClosedException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatRoomNotFoundException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.InactiveChatUserException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.UnauthorizedChatAccessException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatRoom;
import fit.iuh.kttkpm_nhom15_be.chat.domain.repositories.ChatRepository;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssignChatRoomUseCaseTest {

    @Test
    void executeAssignsRoomForStaffSuccessfully() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        AssignChatRoomUseCase useCase = new AssignChatRoomUseCase(chatRepository, userFacade);

        ChatRoom room = ChatRoom.builder()
                .id("room-1")
                .customerId("customer-1")
                .isClosed(false)
                .build();
        ChatRoom savedRoom = ChatRoom.builder()
                .id("room-1")
                .customerId("customer-1")
                .staffId("staff-1")
                .isClosed(false)
                .build();

        when(userFacade.isUserActive("staff-1")).thenReturn(true);
        when(chatRepository.findRoomById("room-1")).thenReturn(Optional.of(room));
        when(chatRepository.saveRoom(any(ChatRoom.class))).thenReturn(savedRoom);

        ChatRoomDTO result = useCase.execute("room-1", "staff-1");

        assertEquals("staff-1", result.staffId());
        verify(chatRepository).saveRoom(any(ChatRoom.class));
    }

    @Test
    void executeThrowsWhenStaffInactive() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        AssignChatRoomUseCase useCase = new AssignChatRoomUseCase(chatRepository, userFacade);

        when(userFacade.isUserActive("staff-2")).thenReturn(false);

        InactiveChatUserException ex = assertThrows(InactiveChatUserException.class,
                () -> useCase.execute("room-2", "staff-2"));

        assertEquals("Khong the gui tin nhan. Tai khoan khong hoat dong: staff-2", ex.getMessage());
        verify(chatRepository, never()).findRoomById(any());
    }

    @Test
    void executeThrowsWhenRoomNotFound() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        AssignChatRoomUseCase useCase = new AssignChatRoomUseCase(chatRepository, userFacade);

        when(userFacade.isUserActive("staff-3")).thenReturn(true);
        when(chatRepository.findRoomById("missing-room")).thenReturn(Optional.empty());

        ChatRoomNotFoundException ex = assertThrows(ChatRoomNotFoundException.class,
                () -> useCase.execute("missing-room", "staff-3"));

        assertEquals("Khong tim thay phong chat voi ID: missing-room", ex.getMessage());
    }

    @Test
    void executeThrowsWhenRoomClosed() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        AssignChatRoomUseCase useCase = new AssignChatRoomUseCase(chatRepository, userFacade);

        ChatRoom room = ChatRoom.builder()
                .id("room-closed")
                .customerId("customer-1")
                .isClosed(true)
                .build();

        when(userFacade.isUserActive("staff-4")).thenReturn(true);
        when(chatRepository.findRoomById("room-closed")).thenReturn(Optional.of(room));

        ChatRoomClosedException ex = assertThrows(ChatRoomClosedException.class,
                () -> useCase.execute("room-closed", "staff-4"));

        assertEquals("Phong chat da ket thuc: room-closed", ex.getMessage());
    }

    @Test
    void executeThrowsWhenRoomAssignedToAnotherStaff() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        AssignChatRoomUseCase useCase = new AssignChatRoomUseCase(chatRepository, userFacade);

        ChatRoom room = ChatRoom.builder()
                .id("room-3")
                .customerId("customer-3")
                .staffId("staff-current")
                .isClosed(false)
                .build();

        when(userFacade.isUserActive("staff-other")).thenReturn(true);
        when(chatRepository.findRoomById("room-3")).thenReturn(Optional.of(room));

        UnauthorizedChatAccessException ex = assertThrows(UnauthorizedChatAccessException.class,
                () -> useCase.execute("room-3", "staff-other"));

        assertEquals("Nguoi dung staff-other khong co quyen thao tac voi phong chat: room-3", ex.getMessage());
        verify(chatRepository, never()).saveRoom(any(ChatRoom.class));
    }
}
