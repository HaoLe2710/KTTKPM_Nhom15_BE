package fit.iuh.kttkpm_nhom15_be.shared.presentation.advice;

import fit.iuh.kttkpm_nhom15_be.auth.domain.exceptions.AccountInactiveException;
import fit.iuh.kttkpm_nhom15_be.auth.domain.exceptions.InvalidCredentialsException;
import fit.iuh.kttkpm_nhom15_be.auth.domain.exceptions.InvalidOtpException;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.exceptions.ProductUnavailableException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatMessageValidationException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatRoomClosedException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.ChatRoomNotFoundException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.InactiveChatUserException;
import fit.iuh.kttkpm_nhom15_be.chat.domain.exceptions.UnauthorizedChatAccessException;
import fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions.InvalidOrderStateTransitionException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiConflictException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiForbiddenException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiNotFoundException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiUnauthorizedException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.exceptions.PromotionNotApplicableException;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.exceptions.PromotionNotFoundException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.InvalidRatingException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.OrderNotCompletedException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.ReviewAlreadyExistsException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.ReviewNotFoundException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.UnauthorizedReviewAccessException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.ActionNotAllowedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.AddressNotFoundException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.DuplicateUserException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(ApiValidationException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(ApiValidationException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(ApiConflictException.class)
  public ResponseEntity<ApiErrorResponse> handleConflict(ApiConflictException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, "CONFLICT_ERROR", ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(ApiNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(ApiNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(ApiUnauthorizedException.class)
  public ResponseEntity<ApiErrorResponse> handleUnauthorized(ApiUnauthorizedException ex, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(ApiForbiddenException.class)
  public ResponseEntity<ApiErrorResponse> handleForbidden(ApiForbiddenException ex, HttpServletRequest request) {
    return build(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(AccountInactiveException.class)
  public ResponseEntity<ApiErrorResponse> handleAccountInactive(AccountInactiveException ex, HttpServletRequest request) {
    return build(HttpStatus.FORBIDDEN, "ACCOUNT_INACTIVE", ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(InvalidOtpException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidOtp(InvalidOtpException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "INVALID_OTP", ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler({
    UserNotFoundException.class,
    AddressNotFoundException.class,
    fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions.OrderNotFoundException.class,
    fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.OrderNotFoundException.class,
    ReviewNotFoundException.class,
    ChatRoomNotFoundException.class,
    PromotionNotFoundException.class
  })
  public ResponseEntity<ApiErrorResponse> handleDomainNotFound(RuntimeException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, resolveCode(ex), ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler({
    DuplicateUserException.class,
    ReviewAlreadyExistsException.class,
    ProductUnavailableException.class,
    DataIntegrityViolationException.class
  })
  public ResponseEntity<ApiErrorResponse> handleDomainConflict(RuntimeException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, resolveCode(ex), ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler({
    ActionNotAllowedException.class,
    ChatRoomClosedException.class,
    InactiveChatUserException.class,
    UnauthorizedChatAccessException.class,
    UnauthorizedReviewAccessException.class,
    AccessDeniedException.class
  })
  public ResponseEntity<ApiErrorResponse> handleDomainForbidden(RuntimeException ex, HttpServletRequest request) {
    return build(HttpStatus.FORBIDDEN, resolveCode(ex), ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler({
    InvalidOrderStateTransitionException.class,
    PromotionNotApplicableException.class,
    InvalidRatingException.class,
    ChatMessageValidationException.class,
    IllegalArgumentException.class,
    NoSuchElementException.class
  })
  public ResponseEntity<ApiErrorResponse> handleDomainBadRequest(RuntimeException ex, HttpServletRequest request) {
    HttpStatus status = ex instanceof NoSuchElementException ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
    return build(status, resolveCode(ex), ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(OrderNotCompletedException.class)
  public ResponseEntity<ApiErrorResponse> handleOrderNotCompleted(OrderNotCompletedException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, "ORDER_NOT_COMPLETED", ex.getMessage(), request.getRequestURI());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<ApiErrorDetail> details = new ArrayList<>();
    ex.getBindingResult().getFieldErrors().forEach(error ->
      details.add(new ApiErrorDetail(error.getField(), error.getDefaultMessage()))
    );
    ex.getBindingResult().getGlobalErrors().forEach(error ->
      details.add(new ApiErrorDetail(error.getObjectName(), error.getDefaultMessage()))
    );
    return build(
      HttpStatus.BAD_REQUEST,
      "VALIDATION_ERROR",
      joinDetailMessages(details, "Request validation failed."),
      request.getRequestURI(),
      details
    );
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
    List<ApiErrorDetail> details = ex.getConstraintViolations().stream()
      .map(violation -> new ApiErrorDetail(extractLeafNode(violation.getPropertyPath().toString()), violation.getMessage()))
      .toList();
    return build(
      HttpStatus.BAD_REQUEST,
      "VALIDATION_ERROR",
      joinDetailMessages(details, "Constraint validation failed."),
      request.getRequestURI(),
      details
    );
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
    ApiErrorDetail detail = new ApiErrorDetail(ex.getParameterName(), "Request parameter is required.");
    return build(
      HttpStatus.BAD_REQUEST,
      "MISSING_REQUEST_PARAMETER",
      ex.getParameterName() + ": Request parameter is required.",
      request.getRequestURI(),
      List.of(detail)
    );
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ApiErrorResponse> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest request) {
    ApiErrorDetail detail = new ApiErrorDetail(ex.getHeaderName(), "Request header is required.");
    return build(
      HttpStatus.BAD_REQUEST,
      "MISSING_REQUEST_HEADER",
      ex.getHeaderName() + ": Request header is required.",
      request.getRequestURI(),
      List.of(detail)
    );
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    String field = ex.getName() == null ? "request" : ex.getName();
    String message = field + ": Invalid value '" + ex.getValue() + "'.";
    return build(
      HttpStatus.BAD_REQUEST,
      "TYPE_MISMATCH",
      message,
      request.getRequestURI(),
      List.of(new ApiErrorDetail(field, message))
    );
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
    return build(
      HttpStatus.BAD_REQUEST,
      "MALFORMED_REQUEST_BODY",
      "Request body is missing or malformed.",
      request.getRequestURI()
    );
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    return build(status, "RESPONSE_STATUS_ERROR", ex.getReason(), request.getRequestURI());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
    log.error("Unhandled exception while processing {} {}", request.getMethod(), request.getRequestURI(), ex);
    return build(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "INTERNAL_SERVER_ERROR",
      "An unexpected server error occurred. Please try again later.",
      request.getRequestURI()
    );
  }

  private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String code, String message, String path) {
    return build(status, code, message, path, List.of());
  }

  private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String code, String message, String path, List<ApiErrorDetail> details) {
    ApiErrorResponse body = new ApiErrorResponse(
      false,
      java.time.Instant.now(),
      status.value(),
      status.getReasonPhrase(),
      code,
      message,
      path,
      details
    );
    return ResponseEntity.status(status).body(body);
  }

  private String joinDetailMessages(List<ApiErrorDetail> details, String fallbackMessage) {
    String joined = details.stream()
      .map(detail -> detail.field() + ": " + detail.message())
      .collect(Collectors.joining("; "));
    return joined.isBlank() ? fallbackMessage : joined;
  }

  private String extractLeafNode(String propertyPath) {
    if (propertyPath == null || propertyPath.isBlank()) {
      return "request";
    }
    int separatorIndex = propertyPath.lastIndexOf('.');
    return separatorIndex >= 0 ? propertyPath.substring(separatorIndex + 1) : propertyPath;
  }

  private String resolveCode(Exception ex) {
    if (ex instanceof UserNotFoundException) {
      return "USER_NOT_FOUND";
    }
    if (ex instanceof AddressNotFoundException) {
      return "ADDRESS_NOT_FOUND";
    }
    if (ex instanceof fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions.OrderNotFoundException) {
      return "ORDER_NOT_FOUND";
    }
    if (ex instanceof fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.OrderNotFoundException) {
      return "ORDER_NOT_FOUND";
    }
    if (ex instanceof ReviewNotFoundException) {
      return "REVIEW_NOT_FOUND";
    }
    if (ex instanceof ChatRoomNotFoundException) {
      return "CHAT_ROOM_NOT_FOUND";
    }
    if (ex instanceof PromotionNotFoundException) {
      return "PROMOTION_NOT_FOUND";
    }
    if (ex instanceof DuplicateUserException) {
      return "DUPLICATE_USER";
    }
    if (ex instanceof ReviewAlreadyExistsException) {
      return "REVIEW_ALREADY_EXISTS";
    }
    if (ex instanceof ProductUnavailableException) {
      return "PRODUCT_UNAVAILABLE";
    }
    if (ex instanceof DataIntegrityViolationException) {
      return "DATA_INTEGRITY_VIOLATION";
    }
    if (ex instanceof ActionNotAllowedException) {
      return "ACTION_NOT_ALLOWED";
    }
    if (ex instanceof ChatRoomClosedException) {
      return "CHAT_ROOM_CLOSED";
    }
    if (ex instanceof InactiveChatUserException) {
      return "INACTIVE_CHAT_USER";
    }
    if (ex instanceof UnauthorizedChatAccessException) {
      return "UNAUTHORIZED_CHAT_ACCESS";
    }
    if (ex instanceof UnauthorizedReviewAccessException) {
      return "UNAUTHORIZED_REVIEW_ACCESS";
    }
    if (ex instanceof AccessDeniedException) {
      return "ACCESS_DENIED";
    }
    if (ex instanceof InvalidOrderStateTransitionException) {
      return "INVALID_ORDER_STATE";
    }
    if (ex instanceof PromotionNotApplicableException) {
      return "PROMOTION_NOT_APPLICABLE";
    }
    if (ex instanceof InvalidRatingException) {
      return "INVALID_RATING";
    }
    if (ex instanceof ChatMessageValidationException) {
      return "CHAT_MESSAGE_INVALID";
    }
    if (ex instanceof IllegalArgumentException) {
      return "INVALID_ARGUMENT";
    }
    if (ex instanceof NoSuchElementException) {
      return "RESOURCE_NOT_FOUND";
    }
    return "BUSINESS_ERROR";
  }
}
