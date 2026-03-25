package fit.iuh.kttkpm_nhom15_be.chat.domain.models;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatRoom {
    private String id;
    private String customerId;
    private String staffId;
    private boolean isClosed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}