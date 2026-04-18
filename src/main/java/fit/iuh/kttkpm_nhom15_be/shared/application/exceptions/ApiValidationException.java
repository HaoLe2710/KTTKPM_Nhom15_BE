package fit.iuh.kttkpm_nhom15_be.shared.application.exceptions;

public class ApiValidationException extends RuntimeException {

  public ApiValidationException(String message) {
    super(message);
  }
}
