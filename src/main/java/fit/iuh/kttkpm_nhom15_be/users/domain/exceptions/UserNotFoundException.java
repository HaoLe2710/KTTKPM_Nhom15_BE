package fit.iuh.kttkpm_nhom15_be.users.domain.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String id) { super("Không tìm thấy tài khoản với ID: " + id); }
}