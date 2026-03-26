package fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions;

public class InactiveChatUserException extends RuntimeException {
    public InactiveChatUserException(String userId) {
        super("Khong the gui tin nhan. Tai khoan khong hoat dong: " + userId);
    }
}
