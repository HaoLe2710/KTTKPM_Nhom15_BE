package fit.iuh.kttkpm_nhom15_be.shared.application.exceptions;

public class ApiNotFoundException extends RuntimeException {

  public ApiNotFoundException(String message) {
    super(message);
  }
}
