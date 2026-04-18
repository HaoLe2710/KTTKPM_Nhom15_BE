package fit.iuh.kttkpm_nhom15_be.chat.application.usecases;

import fit.iuh.kttkpm_nhom15_be.chat.application.commands.SendMessageCommand;
import fit.iuh.kttkpm_nhom15_be.chat.application.dto.MessageDTO;
import fit.iuh.kttkpm_nhom15_be.chat.application.events.ChatMessageSentEvent;
import fit.iuh.kttkpm_nhom15_be.chat.application.support.ChatMessagePayloadSupport;
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

class CustomerSendMessageUseCaseTest {

    @Test
    void executeSendsMessageToOwnedRoomSuccessfully() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        CustomerSendMessageUseCase useCase = new CustomerSendMessageUseCase(chatRepository, userFacade, eventPublisher, new ChatMessagePayloadSupport());

        ChatRoom room = ChatRoom.builder()
                .id("room-1")
                .customerId("customer-1")
                .isClosed(false)
                .build();
        ChatMessage savedMessage = ChatMessage.builder()
                .id("msg-1")
                .roomId("room-1")
                .senderId("customer-1")
                .type(ChatMessageType.TEXT)
                .content("Toi muon mua san pham nay")
                .build();

        when(userFacade.isUserActive("customer-1")).thenReturn(true);
        when(chatRepository.findRoomById("room-1")).thenReturn(Optional.of(room));
        when(chatRepository.saveMessage(any(ChatMessage.class))).thenReturn(savedMessage);

        MessageDTO result = useCase.execute(textCommand("room-1", "customer-1", "Toi muon mua san pham nay"));

        assertEquals("msg-1", result.id());
        verify(eventPublisher).publishEvent(any(ChatMessageSentEvent.class));
    }

    @Test
    void executeThrowsWhenCustomerSendsToAnotherCustomersRoom() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        CustomerSendMessageUseCase useCase = new CustomerSendMessageUseCase(chatRepository, userFacade, eventPublisher, new ChatMessagePayloadSupport());

        ChatRoom room = ChatRoom.builder()
                .id("room-2")
                .customerId("customer-owner")
                .isClosed(false)
                .build();

        when(userFacade.isUserActive("customer-other")).thenReturn(true);
        when(chatRepository.findRoomById("room-2")).thenReturn(Optional.of(room));

        UnauthorizedChatAccessException ex = assertThrows(UnauthorizedChatAccessException.class,
                () -> useCase.execute(textCommand("room-2", "customer-other", "Tin nhan sai room")));

        assertEquals("Nguoi dung customer-other khong co quyen thao tac voi phong chat: room-2", ex.getMessage());
        verify(chatRepository, never()).saveMessage(any());
    }

    @Test
    void executeSendsProductLinkMessage() {
        ChatRepository chatRepository = Mockito.mock(ChatRepository.class);
        UserFacade userFacade = Mockito.mock(UserFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        CustomerSendMessageUseCase useCase = new CustomerSendMessageUseCase(chatRepository, userFacade, eventPublisher, new ChatMessagePayloadSupport());

        ChatRoom room = ChatRoom.builder().id("room-product").customerId("customer-3").isClosed(false).build();
        ChatMessage savedMessage = ChatMessage.builder()
                .id("msg-product")
                .roomId("room-product")
                .senderId("customer-3")
                .type(ChatMessageType.PRODUCT_LINK)
                .linkUrl("https://shop.example.com/products/coffee")
                .productName("Arabica Coffee")
                .build();

        when(userFacade.isUserActive("customer-3")).thenReturn(true);
        when(chatRepository.findRoomById("room-product")).thenReturn(Optional.of(room));
        when(chatRepository.saveMessage(any(ChatMessage.class))).thenReturn(savedMessage);

        MessageDTO result = useCase.execute(new SendMessageCommand(
                "room-product", "customer-3", ChatMessageType.PRODUCT_LINK, "Anh chi xem giup em san pham nay",
                null, "https://shop.example.com/products/coffee", "product-1", "variant-1",
                "Arabica Coffee", "https://cdn.example.com/products/coffee.png", null
        ));

        assertEquals(ChatMessageType.PRODUCT_LINK, result.type());
        assertEquals("Arabica Coffee", result.productName());
        verify(eventPublisher).publishEvent(any(ChatMessageSentEvent.class));
    }

    private SendMessageCommand textCommand(String roomId, String senderId, String content) {
        return new SendMessageCommand(roomId, senderId, ChatMessageType.TEXT, content,
                null, null, null, null, null, null, null);
    }
}
