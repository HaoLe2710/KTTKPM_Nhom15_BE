package fit.iuh.kttkpm_nhom15_be.shared.application.exceptions;

public class ApiUnauthorizedException extends RuntimeException {

  public ApiUnauthorizedException(String message) {
    super(message);
  }
}
