package fit.iuh.kttkpm_nhom15_be.chat.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.chat.application.commands.SendMessageCommand;
import fit.iuh.kttkpm_nhom15_be.chat.application.usecases.GetChatHistoryUseCase;
import fit.iuh.kttkpm_nhom15_be.chat.application.usecases.SendMessageUseCase;
import fit.iuh.kttkpm_nhom15_be.chat.domain.models.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final SendMessageUseCase sendMessageUseCase;
    private final GetChatHistoryUseCase getChatHistoryUseCase;

    // API 1: Lấy lịch sử tin nhắn của 1 phòng chat
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable String roomId) {
        return ResponseEntity.ok(getChatHistoryUseCase.execute(roomId));
    }

    // API 2: Gửi tin nhắn (Cho cả Customer và Staff)
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Map<String, String>> sendMessage(
            @PathVariable String roomId,
            @RequestHeader("X-User-Id") String senderId, // Giả lập lấy ID từ Token đăng nhập
            @RequestBody Map<String, String> payload
    ) {
        String content = payload.get("content");

        ChatMessage msg = sendMessageUseCase.execute(new SendMessageCommand(roomId, senderId, content));

        return ResponseEntity.ok(Map.of("messageId", msg.getId(), "status", "Sent"));
    }
}