package fit.iuh.kttkpm_nhom15_be.users.domain.exceptions;

public class ActionNotAllowedException extends RuntimeException {
    public ActionNotAllowedException(String message) { super(message); }
}