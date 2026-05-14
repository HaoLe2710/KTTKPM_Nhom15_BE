package fit.iuh.kttkpm_nhom15_be.chat.application.usecases;

import fit.iuh.kttkpm_nhom15_be.chat.application.dto.ChatRoomDTO;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.InactiveChatUserException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatRoom;
import fit.iuh.kttkpm_nhom15_be.chat.domain.repositories.ChatRepository;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateOrGetChatRoomUseCaseTest {

    @Test
    void executeReturnsExistingActiveRoom() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        CreateOrGetChatRoomUseCase useCase = new CreateOrGetChatRoomUseCase(chatRepository, userFacade);

        ChatRoom room = ChatRoom.builder()
                .id("room-1")
                .customerId("customer-1")
                .staffId("staff-1")
                .isClosed(false)
                .build();

        when(userFacade.isUserActive("customer-1")).thenReturn(true);
        when(chatRepository.findActiveRoomByCustomer("customer-1")).thenReturn(Optional.of(room));

        ChatRoomDTO result = useCase.execute("customer-1");

        assertEquals("room-1", result.id());
        assertEquals("customer-1", result.customerId());
        verify(chatRepository, never()).saveRoom(any(ChatRoom.class));
    }

    @Test
    void executeCreatesNewRoomWhenCustomerHasNoActiveRoom() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        CreateOrGetChatRoomUseCase useCase = new CreateOrGetChatRoomUseCase(chatRepository, userFacade);

        ChatRoom savedRoom = ChatRoom.builder()
                .id("room-new")
                .customerId("customer-2")
                .isClosed(false)
                .build();

        when(userFacade.isUserActive("customer-2")).thenReturn(true);
        when(chatRepository.findActiveRoomByCustomer("customer-2")).thenReturn(Optional.empty());
        when(chatRepository.saveRoom(any(ChatRoom.class))).thenReturn(savedRoom);

        ChatRoomDTO result = useCase.execute("customer-2");

        ArgumentCaptor<ChatRoom> roomCaptor = ArgumentCaptor.forClass(ChatRoom.class);
        verify(chatRepository).saveRoom(roomCaptor.capture());
        assertEquals("customer-2", roomCaptor.getValue().getCustomerId());
        assertEquals("room-new", result.id());
    }

    @Test
    void executeThrowsWhenCustomerInactive() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        CreateOrGetChatRoomUseCase useCase = new CreateOrGetChatRoomUseCase(chatRepository, userFacade);

        when(userFacade.isUserActive("customer-3")).thenReturn(false);

        InactiveChatUserException ex = assertThrows(InactiveChatUserException.class,
                () -> useCase.execute("customer-3"));

        assertEquals("Khong the gui tin nhan. Tai khoan khong hoat dong: customer-3", ex.getMessage());
        verify(chatRepository, never()).findActiveRoomByCustomer(any());
    }
}
