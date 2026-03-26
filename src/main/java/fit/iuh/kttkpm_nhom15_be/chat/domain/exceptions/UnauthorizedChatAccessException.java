package fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions;

public class UnauthorizedChatAccessException extends RuntimeException {
    public UnauthorizedChatAccessException(String roomId, String userId) {
        super("Nguoi dung " + userId + " khong co quyen thao tac voi phong chat: " + roomId);
    }
}
