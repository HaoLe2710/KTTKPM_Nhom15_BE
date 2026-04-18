package fit.iuh.kttkpm_nhom15_be.chat.application.usecases;

import fit.iuh.kttkpm_nhom15_be.chat.application.commands.SendMessageCommand;
import fit.iuh.kttkpm_nhom15_be.chat.application.dto.MessageDTO;
import fit.iuh.kttkpm_nhom15_be.chat.application.events.ChatMessageSentEvent;
import fit.iuh.kttkpm_nhom15_be.chat.application.support.ChatMessagePayloadSupport;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatMessageValidationException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatRoomClosedException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatRoomNotFoundException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.InactiveChatUserException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatMessage;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatMessageType;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatRoom;
import fit.iuh.kttkpm_nhom15_be.chat.domain.repositories.ChatRepository;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SendMessageUseCaseTest {

    @Test
    void executeCreatesRoomWhenRoomIdIsBlank() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        SendMessageUseCase useCase = new SendMessageUseCase(chatRepository, userFacade, eventPublisher, new ChatMessagePayloadSupport());

        ChatRoom room = ChatRoom.builder()
                .id("room-1")
                .customerId("user-1")
                .isClosed(false)
                .build();
        ChatMessage savedMessage = ChatMessage.builder()
                .id("msg-1")
                .roomId("room-1")
                .senderId("user-1")
                .type(ChatMessageType.TEXT)
                .content("Xin chao")
                .build();

        when(userFacade.isUserActive("user-1")).thenReturn(true);
        when(chatRepository.findActiveRoomByCustomer("user-1")).thenReturn(Optional.empty());
        when(chatRepository.saveRoom(any(ChatRoom.class))).thenReturn(room);
        when(chatRepository.saveMessage(any(ChatMessage.class))).thenReturn(savedMessage);

        MessageDTO result = useCase.execute(textCommand("", "user-1", "Xin chao"));

        ArgumentCaptor<ChatRoom> roomCaptor = ArgumentCaptor.forClass(ChatRoom.class);
        verify(chatRepository).saveRoom(roomCaptor.capture());
        verify(chatRepository).saveMessage(any(ChatMessage.class));
        verify(eventPublisher).publishEvent(any(ChatMessageSentEvent.class));

        assertEquals("user-1", roomCaptor.getValue().getCustomerId());
        assertEquals("room-1", result.roomId());
        assertEquals("msg-1", result.id());
    }

    @Test
    void executeUsesExistingRoomWhenRoomIdIsProvided() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        SendMessageUseCase useCase = new SendMessageUseCase(chatRepository, userFacade, eventPublisher, new ChatMessagePayloadSupport());

        ChatRoom room = ChatRoom.builder()
                .id("room-2")
                .customerId("user-1")
                .isClosed(false)
                .build();
        ChatMessage savedMessage = ChatMessage.builder()
                .id("msg-2")
                .roomId("room-2")
                .senderId("user-1")
                .type(ChatMessageType.TEXT)
                .content("Toi can tu van")
                .build();

        when(userFacade.isUserActive("user-1")).thenReturn(true);
        when(chatRepository.findRoomById("room-2")).thenReturn(Optional.of(room));
        when(chatRepository.saveMessage(any(ChatMessage.class))).thenReturn(savedMessage);

        MessageDTO result = useCase.execute(textCommand("room-2", "user-1", "Toi can tu van"));

        verify(chatRepository, never()).saveRoom(any(ChatRoom.class));
        verify(chatRepository).saveMessage(any(ChatMessage.class));
        assertEquals("room-2", result.roomId());
    }

    @Test
    void executeThrowsWhenContentIsBlank() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        SendMessageUseCase useCase = new SendMessageUseCase(chatRepository, userFacade, eventPublisher, new ChatMessagePayloadSupport());

        ChatMessageValidationException ex = assertThrows(ChatMessageValidationException.class,
                () -> useCase.execute(textCommand("room-1", "user-1", "  ")));

        assertEquals("Noi dung tin nhan khong duoc de trong.", ex.getMessage());
        verify(userFacade, never()).isUserActive(any());
    }

    @Test
    void executeThrowsWhenUserIsInactive() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        SendMessageUseCase useCase = new SendMessageUseCase(chatRepository, userFacade, eventPublisher, new ChatMessagePayloadSupport());

        when(userFacade.isUserActive("user-1")).thenReturn(false);

        InactiveChatUserException ex = assertThrows(InactiveChatUserException.class,
                () -> useCase.execute(textCommand("room-1", "user-1", "Xin chao")));

        assertEquals("Khong the gui tin nhan. Tai khoan khong hoat dong: user-1", ex.getMessage());
        verify(chatRepository, never()).findRoomById(any());
    }

    @Test
    void executeThrowsWhenRoomDoesNotExist() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        SendMessageUseCase useCase = new SendMessageUseCase(chatRepository, userFacade, eventPublisher, new ChatMessagePayloadSupport());

        when(userFacade.isUserActive("user-1")).thenReturn(true);
        when(chatRepository.findRoomById("missing-room")).thenReturn(Optional.empty());

        ChatRoomNotFoundException ex = assertThrows(ChatRoomNotFoundException.class,
                () -> useCase.execute(textCommand("missing-room", "user-1", "Xin chao")));

        assertEquals("Khong tim thay phong chat voi ID: missing-room", ex.getMessage());
        verify(chatRepository, never()).saveMessage(any());
    }

    @Test
    void executeThrowsWhenRoomIsClosed() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        SendMessageUseCase useCase = new SendMessageUseCase(chatRepository, userFacade, eventPublisher, new ChatMessagePayloadSupport());

        ChatRoom room = ChatRoom.builder()
                .id("room-closed")
                .customerId("user-1")
                .isClosed(true)
                .build();

        when(userFacade.isUserActive("user-1")).thenReturn(true);
        when(chatRepository.findRoomById("room-closed")).thenReturn(Optional.of(room));

        ChatRoomClosedException ex = assertThrows(ChatRoomClosedException.class,
                () -> useCase.execute(textCommand("room-closed", "user-1", "Xin chao")));

        assertEquals("Phong chat da ket thuc: room-closed", ex.getMessage());
        verify(chatRepository, never()).saveMessage(any());
    }

    @Test
    void executeSendsImageMessage() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        SendMessageUseCase useCase = new SendMessageUseCase(chatRepository, userFacade, eventPublisher, new ChatMessagePayloadSupport());

        ChatRoom room = ChatRoom.builder().id("room-image").customerId("user-2").isClosed(false).build();
        ChatMessage savedMessage = ChatMessage.builder()
                .id("msg-image")
                .roomId("room-image")
                .senderId("user-2")
                .type(ChatMessageType.IMAGE)
                .imageUrl("https://cdn.example.com/chat/image.png")
                .build();

        when(userFacade.isUserActive("user-2")).thenReturn(true);
        when(chatRepository.findRoomById("room-image")).thenReturn(Optional.of(room));
        when(chatRepository.saveMessage(any(ChatMessage.class))).thenReturn(savedMessage);

        MessageDTO result = useCase.execute(new SendMessageCommand(
                "room-image", "user-2", ChatMessageType.IMAGE, null,
                "https://cdn.example.com/chat/image.png", null, null, null, null, null, null
        ));

        assertEquals(ChatMessageType.IMAGE, result.type());
        assertEquals("https://cdn.example.com/chat/image.png", result.imageUrl());
    }

    private SendMessageCommand textCommand(String roomId, String senderId, String content) {
        return new SendMessageCommand(roomId, senderId, ChatMessageType.TEXT, content,
                null, null, null, null, null, null, null);
    }
}
