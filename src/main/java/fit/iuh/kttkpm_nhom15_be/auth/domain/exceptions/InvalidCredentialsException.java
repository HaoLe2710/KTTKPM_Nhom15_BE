package fit.iuh.kttkpm_nhom15_be.auth.domain.exceptions;

public class InvalidCredentialsException extends RuntimeException {

  public InvalidCredentialsException(String message) {
    super(message);
  }
}
