package fit.iuh.kttkpm_nhom15_be.chat.application.usecases;

import fit.iuh.kttkpm_nhom15_be.chat.application.commands.SendMessageCommand;
import fit.iuh.kttkpm_nhom15_be.chat.application.dto.MessageDTO;
import fit.iuh.kttkpm_nhom15_be.chat.application.events.ChatMessageSentEvent;
import fit.iuh.kttkpm_nhom15_be.chat.application.support.ChatMessagePayloadSupport;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatRoomClosedException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatRoomNotFoundException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.UnauthorizedChatAccessException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatMessage;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatMessageType;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatRoom;
import fit.iuh.kttkpm_nhom15_be.chat.domain.repositories.ChatRepository;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StaffReplyMessageUseCaseTest {

    @Test
    void executeAutoAssignsStaffWhenRoomHasNoStaff() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        StaffReplyMessageUseCase useCase = new StaffReplyMessageUseCase(chatRepository, userFacade, eventPublisher, new ChatMessagePayloadSupport());

        ChatRoom room = ChatRoom.builder()
                .id("room-1")
                .customerId("customer-1")
                .isClosed(false)
                .build();
        ChatRoom assignedRoom = ChatRoom.builder()
                .id("room-1")
                .customerId("customer-1")
                .staffId("staff-1")
                .isClosed(false)
                .build();
        ChatMessage savedMessage = ChatMessage.builder()
                .id("msg-1")
                .roomId("room-1")
                .senderId("staff-1")
                .type(ChatMessageType.TEXT)
                .content("Anh chị cần hỗ trợ sản phẩm nào?")
                .build();

        when(userFacade.isUserActive("staff-1")).thenReturn(true);
        when(chatRepository.findRoomById("room-1")).thenReturn(Optional.of(room));
        when(chatRepository.saveRoom(any(ChatRoom.class))).thenReturn(assignedRoom);
        when(chatRepository.saveMessage(any(ChatMessage.class))).thenReturn(savedMessage);

        MessageDTO result = useCase.execute(textCommand("room-1", "staff-1", "Anh chị cần hỗ trợ sản phẩm nào?"));

        assertEquals("msg-1", result.id());
        verify(chatRepository).saveRoom(any(ChatRoom.class));
        verify(eventPublisher).publishEvent(any(ChatMessageSentEvent.class));
    }

    @Test
    void executeThrowsWhenRoomAssignedToAnotherStaff() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        StaffReplyMessageUseCase useCase = new StaffReplyMessageUseCase(chatRepository, userFacade, eventPublisher, new ChatMessagePayloadSupport());

        ChatRoom room = ChatRoom.builder()
                .id("room-2")
                .customerId("customer-2")
                .staffId("staff-current")
                .isClosed(false)
                .build();

        when(userFacade.isUserActive("staff-other")).thenReturn(true);
        when(chatRepository.findRoomById("room-2")).thenReturn(Optional.of(room));

        UnauthorizedChatAccessException ex = assertThrows(UnauthorizedChatAccessException.class,
                () -> useCase.execute(textCommand("room-2", "staff-other", "Toi se ho tro anh chi")));

        assertEquals("Người dùng staff-other không có quyền thao tác với phòng chat: room-2", ex.getMessage());
        verify(chatRepository, never()).saveMessage(any());
    }

    @Test
    void executeThrowsWhenRoomNotFound() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        StaffReplyMessageUseCase useCase = new StaffReplyMessageUseCase(chatRepository, userFacade, eventPublisher, new ChatMessagePayloadSupport());

        when(userFacade.isUserActive("staff-3")).thenReturn(true);
        when(chatRepository.findRoomById("missing-room")).thenReturn(Optional.empty());

        ChatRoomNotFoundException ex = assertThrows(ChatRoomNotFoundException.class,
                () -> useCase.execute(textCommand("missing-room", "staff-3", "Xin chao")));

        assertEquals("Không tìm thấy phòng chat với ID: missing-room", ex.getMessage());
    }

    @Test
    void executeThrowsWhenRoomClosed() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        StaffReplyMessageUseCase useCase = new StaffReplyMessageUseCase(chatRepository, userFacade, eventPublisher, new ChatMessagePayloadSupport());

        ChatRoom room = ChatRoom.builder()
                .id("room-closed")
                .customerId("customer-4")
                .staffId("staff-4")
                .isClosed(true)
                .build();

        when(userFacade.isUserActive("staff-4")).thenReturn(true);
        when(chatRepository.findRoomById("room-closed")).thenReturn(Optional.of(room));

        ChatRoomClosedException ex = assertThrows(ChatRoomClosedException.class,
                () -> useCase.execute(textCommand("room-closed", "staff-4", "Phong nay da dong")));

        assertEquals("Phong chat da ket thuc: room-closed", ex.getMessage());
    }

    @Test
    void executeSendsImageReply() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        StaffReplyMessageUseCase useCase = new StaffReplyMessageUseCase(chatRepository, userFacade, eventPublisher, new ChatMessagePayloadSupport());

        ChatRoom room = ChatRoom.builder()
                .id("room-image")
                .customerId("customer-5")
                .staffId("staff-5")
                .isClosed(false)
                .build();
        ChatMessage savedMessage = ChatMessage.builder()
                .id("msg-image")
                .roomId("room-image")
                .senderId("staff-5")
                .type(ChatMessageType.IMAGE)
                .imageUrl("https://cdn.example.com/reply/image.png")
                .build();

        when(userFacade.isUserActive("staff-5")).thenReturn(true);
        when(chatRepository.findRoomById("room-image")).thenReturn(Optional.of(room));
        when(chatRepository.saveMessage(any(ChatMessage.class))).thenReturn(savedMessage);

        MessageDTO result = useCase.execute(new SendMessageCommand(
                "room-image", "staff-5", ChatMessageType.IMAGE, null,
                "https://cdn.example.com/reply/image.png", null, null, null, null, null, null, null
        ));

        assertEquals(ChatMessageType.IMAGE, result.type());
        assertEquals("https://cdn.example.com/reply/image.png", result.imageUrl());
    }

    private SendMessageCommand textCommand(String roomId, String senderId, String content) {
        return new SendMessageCommand(roomId, senderId, ChatMessageType.TEXT, content,
                null, null, null, null, null, null, null, null);
    }
}
