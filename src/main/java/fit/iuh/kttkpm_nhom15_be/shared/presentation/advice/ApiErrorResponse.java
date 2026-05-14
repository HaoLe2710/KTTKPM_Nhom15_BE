package fit.iuh.kttkpm_nhom15_be.shared.presentation.advice;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
  boolean success,
  Instant timestamp,
  int status,
  String error,
  String code,
  String message,
  String path,
  List<ApiErrorDetail> details
) {
}
