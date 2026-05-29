package fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions;

public class UnauthorizedChatAccessException extends RuntimeException {
    public UnauthorizedChatAccessException(String roomId, String userId) {
        super("Người dùng " + userId + " không có quyền thao tác với phòng chat: " + roomId);
    }
}
