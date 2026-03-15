package fit.iuh.kttkpm_nhom15_be.chat.domain.models;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String id;
    private String senderId;
    private String content;
}