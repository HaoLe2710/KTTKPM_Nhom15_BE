package fit.iuh.kttkpm_nhom15_be.shared.presentation.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.ApiSuccessResponse;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.MessageResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiSuccessResponseAdvice implements ResponseBodyAdvice<Object> {

  private final ObjectMapper objectMapper;

  @Override
  public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(Object body,
                                MethodParameter returnType,
                                MediaType selectedContentType,
                                Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                ServerHttpRequest request,
                                ServerHttpResponse response) {
    if (shouldSkip(body, returnType, selectedContentType, request, response)) {
      return body;
    }

    HttpStatusCode statusCode = resolveStatusCode(response);
    String path = request.getURI().getPath();
    String message = resolveMessage(body, returnType, request.getMethod(), statusCode.value());
    Object data = resolveData(body);

    ApiSuccessResponse<Object> envelope = ApiSuccessResponse.of(
      statusCode.value(),
      message,
      path,
      data
    );

    if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
      response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
      return writeAsJson(envelope);
    }

    return envelope;
  }

  private boolean shouldSkip(Object body,
                             MethodParameter returnType,
                             MediaType selectedContentType,
                             ServerHttpRequest request,
                             ServerHttpResponse response) {
    HttpStatusCode statusCode = resolveStatusCode(response);

    if (statusCode == null || !statusCode.is2xxSuccessful()) {
      return true;
    }

    String path = request.getURI().getPath();
    if (path == null || !path.startsWith("/api/")) {
      return true;
    }

    if (hasSkipAnnotation(returnType)) {
      return true;
    }

    if (body instanceof ApiErrorResponse
      || body instanceof ApiSuccessResponse<?>
      || body instanceof byte[]
      || body instanceof Resource
      || body instanceof StreamingResponseBody
      || body instanceof ProblemDetail) {
      return true;
    }

    if (selectedContentType == null) {
      return false;
    }

    if (MediaType.APPLICATION_PDF.includes(selectedContentType)
      || MediaType.APPLICATION_OCTET_STREAM.includes(selectedContentType)
      || MediaType.TEXT_EVENT_STREAM.includes(selectedContentType)) {
      return true;
    }

    return false;
  }

  private boolean hasSkipAnnotation(MethodParameter returnType) {
    if (returnType.getMethod() != null
      && AnnotatedElementUtils.hasAnnotation(returnType.getMethod(), SkipSuccessEnvelope.class)) {
      return true;
    }

    return AnnotatedElementUtils.hasAnnotation(returnType.getContainingClass(), SkipSuccessEnvelope.class);
  }

  private String resolveMessage(Object body, MethodParameter returnType, HttpMethod httpMethod, int status) {
    ApiSuccessMessage annotation = findSuccessMessageAnnotation(returnType);
    if (annotation != null && !annotation.value().isBlank()) {
      return annotation.value();
    }

    if (body instanceof MessageResponse messageResponse && messageResponse.message() != null) {
      return messageResponse.message();
    }

    if (body instanceof Map<?, ?> map) {
      Object message = map.get("message");
      if (message instanceof String messageText && !messageText.isBlank()) {
        return messageText;
      }
    }

    if (status == 201) {
      return "Tao moi thanh cong";
    }

    if (status == 202) {
      return "Yeu cau da duoc tiep nhan thanh cong";
    }

    if (httpMethod == null) {
      return "Yeu cau thanh cong";
    }

    if (HttpMethod.GET.equals(httpMethod) || HttpMethod.HEAD.equals(httpMethod)) {
      return "Lay du lieu thanh cong";
    }
    if (HttpMethod.POST.equals(httpMethod)) {
      return "Xu ly thanh cong";
    }
    if (HttpMethod.PUT.equals(httpMethod) || HttpMethod.PATCH.equals(httpMethod)) {
      return "Cap nhat thanh cong";
    }
    if (HttpMethod.DELETE.equals(httpMethod)) {
      return "Xoa thanh cong";
    }
    return "Yeu cau thanh cong";
  }

  private ApiSuccessMessage findSuccessMessageAnnotation(MethodParameter returnType) {
    if (returnType.getMethod() != null) {
      ApiSuccessMessage methodAnnotation =
        AnnotatedElementUtils.findMergedAnnotation(returnType.getMethod(), ApiSuccessMessage.class);
      if (methodAnnotation != null) {
        return methodAnnotation;
      }
    }

    return AnnotatedElementUtils.findMergedAnnotation(returnType.getContainingClass(), ApiSuccessMessage.class);
  }

  private Object resolveData(Object body) {
    if (body == null || body instanceof MessageResponse) {
      return null;
    }

    if (body instanceof Map<?, ?> map && map.size() == 1 && map.containsKey("message")) {
      return null;
    }

    return body;
  }

  private String writeAsJson(ApiSuccessResponse<Object> envelope) {
    try {
      return objectMapper.writeValueAsString(envelope);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Khong the tao success response JSON", exception);
    }
  }

  private HttpStatusCode resolveStatusCode(ServerHttpResponse response) {
    if (response instanceof ServletServerHttpResponse servletResponse) {
      return HttpStatusCode.valueOf(servletResponse.getServletResponse().getStatus());
    }

    return HttpStatus.OK;
  }
}
