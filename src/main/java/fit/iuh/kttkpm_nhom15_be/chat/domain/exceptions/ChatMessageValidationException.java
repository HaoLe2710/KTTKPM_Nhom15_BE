package fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions;

public class ChatMessageValidationException extends RuntimeException {
    public ChatMessageValidationException(String message) {
        super(message);
    }
}
