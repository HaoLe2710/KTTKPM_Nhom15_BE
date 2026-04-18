package fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions;

public class ChatRoomNotFoundException extends RuntimeException {
    public ChatRoomNotFoundException(String roomId) {
        super("Khong tim thay phong chat voi ID: " + roomId);
    }
}
