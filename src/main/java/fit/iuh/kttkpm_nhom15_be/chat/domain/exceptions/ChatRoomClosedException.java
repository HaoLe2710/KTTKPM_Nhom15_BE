package fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions;

public class ChatRoomClosedException extends RuntimeException {
    public ChatRoomClosedException(String roomId) {
        super("Phong chat da ket thuc: " + roomId);
    }
}
