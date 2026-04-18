package fit.iuh.kttkpm_nhom15_be.shared.presentation.advice;

import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiConflictException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiForbiddenException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiNotFoundException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiUnauthorizedException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(ApiValidationException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(ApiValidationException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(ApiConflictException.class)
  public ResponseEntity<Map<String, Object>> handleConflict(ApiConflictException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(ApiNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(ApiNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(ApiUnauthorizedException.class)
  public ResponseEntity<Map<String, Object>> handleUnauthorized(ApiUnauthorizedException ex, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(ApiForbiddenException.class)
  public ResponseEntity<Map<String, Object>> handleForbidden(ApiForbiddenException ex, HttpServletRequest request) {
    return build(HttpStatus.FORBIDDEN, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    String message = ex.getBindingResult().getFieldErrors().stream()
      .map(error -> error.getField() + ": " + error.getDefaultMessage())
      .collect(Collectors.joining("; "));
    return build(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
    return build(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason(), request.getRequestURI());
  }

  private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, String path) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", message);
    body.put("path", path);
    return ResponseEntity.status(status).body(body);
  }
}
