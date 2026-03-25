package fit.iuh.kttkpm_nhom15_be.chat.domain.models;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatMessage {
    private String id;
    private String roomId;
    private String senderId;
    private String content;
    private LocalDateTime sentAt;
}