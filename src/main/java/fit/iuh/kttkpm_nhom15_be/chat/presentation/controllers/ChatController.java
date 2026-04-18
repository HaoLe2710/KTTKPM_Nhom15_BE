package fit.iuh.kttkpm_nhom15_be.chat.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.chat.application.commands.SendMessageCommand;
import fit.iuh.kttkpm_nhom15_be.chat.application.dto.ChatRoomDTO;
import fit.iuh.kttkpm_nhom15_be.chat.application.dto.MessageDTO;
import fit.iuh.kttkpm_nhom15_be.chat.application.usecases.AssignChatRoomUseCase;
import fit.iuh.kttkpm_nhom15_be.chat.application.usecases.CreateOrGetChatRoomUseCase;
import fit.iuh.kttkpm_nhom15_be.chat.application.usecases.CustomerSendMessageUseCase;
import fit.iuh.kttkpm_nhom15_be.chat.application.usecases.GetActiveChatRoomsUseCase;
import fit.iuh.kttkpm_nhom15_be.chat.application.usecases.GetChatHistoryUseCase;
import fit.iuh.kttkpm_nhom15_be.chat.application.usecases.SendMessageUseCase;
import fit.iuh.kttkpm_nhom15_be.chat.application.usecases.StaffReplyMessageUseCase;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatMessageValidationException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatRoomClosedException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatRoomNotFoundException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.InactiveChatUserException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.UnauthorizedChatAccessException;
import fit.iuh.kttkpm_nhom15_be.chat.presentation.requests.SendMessageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final SendMessageUseCase sendMessageUseCase;
    private final CustomerSendMessageUseCase customerSendMessageUseCase;
    private final StaffReplyMessageUseCase staffReplyMessageUseCase;
    private final CreateOrGetChatRoomUseCase createOrGetChatRoomUseCase;
    private final GetActiveChatRoomsUseCase getActiveChatRoomsUseCase;
    private final AssignChatRoomUseCase assignChatRoomUseCase;
    private final GetChatHistoryUseCase getChatHistoryUseCase;

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<MessageDTO>> getChatHistory(@PathVariable String roomId) {
        return ResponseEntity.ok(getChatHistoryUseCase.execute(roomId));
    }

    @GetMapping("/customer/rooms/active")
    public ResponseEntity<ChatRoomDTO> getCustomerActiveRoom(@RequestHeader("X-User-Id") String customerId) {
        return ResponseEntity.ok(createOrGetChatRoomUseCase.execute(customerId));
    }

    @PostMapping("/customer/rooms")
    public ResponseEntity<ChatRoomDTO> createOrGetCustomerRoom(@RequestHeader("X-User-Id") String customerId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createOrGetChatRoomUseCase.execute(customerId));
    }

    @GetMapping("/staff/rooms/active")
    public ResponseEntity<List<ChatRoomDTO>> getActiveRooms() {
        return ResponseEntity.ok(getActiveChatRoomsUseCase.execute());
    }

    @PatchMapping("/staff/rooms/{roomId}/assign")
    public ResponseEntity<ChatRoomDTO> assignRoom(
            @PathVariable String roomId,
            @RequestHeader("X-User-Id") String staffId
    ) {
        return ResponseEntity.ok(assignChatRoomUseCase.execute(roomId, staffId));
    }

    @PostMapping("/customer/messages")
    public ResponseEntity<MessageDTO> customerSendMessage(
            @RequestHeader("X-User-Id") String customerId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        MessageDTO response = customerSendMessageUseCase.execute(
                new SendMessageCommand(
                        request.getRoomId(),
                        customerId,
                        request.getType(),
                        request.getContent(),
                        request.getImageUrl(),
                        request.getLinkUrl(),
                        request.getProductId(),
                        request.getVariantId(),
                        request.getProductName(),
                        request.getProductImageUrl(),
                        request.getProductPrice()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/staff/rooms/{roomId}/messages")
    public ResponseEntity<MessageDTO> staffReplyMessage(
            @PathVariable String roomId,
            @RequestHeader("X-User-Id") String staffId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        MessageDTO response = staffReplyMessageUseCase.execute(
                new SendMessageCommand(
                        roomId,
                        staffId,
                        request.getType(),
                        request.getContent(),
                        request.getImageUrl(),
                        request.getLinkUrl(),
                        request.getProductId(),
                        request.getVariantId(),
                        request.getProductName(),
                        request.getProductImageUrl(),
                        request.getProductPrice()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<MessageDTO> sendMessage(
            @PathVariable String roomId,
            @RequestHeader("X-User-Id") String senderId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        MessageDTO response = sendMessageUseCase.execute(new SendMessageCommand(
                roomId,
                senderId,
                request.getType(),
                request.getContent(),
                request.getImageUrl(),
                request.getLinkUrl(),
                request.getProductId(),
                request.getVariantId(),
                request.getProductName(),
                request.getProductImageUrl(),
                request.getProductPrice()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(ChatRoomNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRoomNotFound(ChatRoomNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler({ChatRoomClosedException.class, InactiveChatUserException.class, UnauthorizedChatAccessException.class})
    public ResponseEntity<Map<String, String>> handleForbidden(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler({ChatMessageValidationException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException validationException
                && validationException.getBindingResult().getFieldError() != null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error",
                    validationException.getBindingResult().getFieldError().getDefaultMessage()
            ));
        }
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
