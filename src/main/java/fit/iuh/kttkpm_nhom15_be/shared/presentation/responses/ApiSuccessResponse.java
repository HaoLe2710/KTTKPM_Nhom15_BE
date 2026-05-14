package fit.iuh.kttkpm_nhom15_be.shared.presentation.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiSuccessResponse<T>(
  boolean success,
  Instant timestamp,
  int status,
  String message,
  String path,
  T data
) {

  public static <T> ApiSuccessResponse<T> of(int status, String message, String path, T data) {
    return new ApiSuccessResponse<>(true, Instant.now(), status, message, path, data);
  }
}
