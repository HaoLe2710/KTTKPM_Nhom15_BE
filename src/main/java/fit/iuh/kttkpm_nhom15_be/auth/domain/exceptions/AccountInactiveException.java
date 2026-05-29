package fit.iuh.kttkpm_nhom15_be.auth.domain.exceptions;

public class AccountInactiveException extends RuntimeException {

  public AccountInactiveException(String message) {
    super(message);
  }
}
