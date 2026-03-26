package fit.iuh.kttkpm_nhom15_be.chat.infrastructure.websocket;

import fit.iuh.kttkpm_nhom15_be.chat.application.events.ChatMessageSentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessageSocketPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handle(ChatMessageSentEvent event) {
        messagingTemplate.convertAndSend("/topic/chat/rooms/" + event.roomId(), event.message());
    }
}
