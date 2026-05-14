package fit.iuh.kttkpm_nhom15_be.auth.domain.exceptions;

public class InvalidOtpException extends RuntimeException {

  public InvalidOtpException(String message) {
    super(message);
  }
}
