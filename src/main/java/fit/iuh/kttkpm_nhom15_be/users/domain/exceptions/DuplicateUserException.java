package fit.iuh.kttkpm_nhom15_be.users.domain.exceptions;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String message) { super(message); }
}
