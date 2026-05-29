package fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions;

public class InactiveChatUserException extends RuntimeException {
    public InactiveChatUserException(String userId) {
        super("Không thể gửi tin nhắn. Tài khoản không hoạt động: " + userId);
    }
}
