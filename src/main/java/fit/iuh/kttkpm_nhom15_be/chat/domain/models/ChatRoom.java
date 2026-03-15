package fit.iuh.kttkpm_nhom15_be.chat.domain.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom {
    private String id;
    private String userId;
    private String staffId;
    private List<ChatMessage> messages;
}