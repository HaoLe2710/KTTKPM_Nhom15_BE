package fit.iuh.kttkpm_nhom15_be.search.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import fit.iuh.kttkpm_nhom15_be.search.application.interfaces.ImageQueryExtractorPort;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class GeminiImageQueryExtractorAdapter implements ImageQueryExtractorPort {

  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

  private final WebClient webClient;
  private final String apiKey;
  private final String model;

  public GeminiImageQueryExtractorAdapter(WebClient.Builder webClientBuilder,
                                          @Value("${gemini.api.key:}") String apiKey,
                                          @Value("${gemini.model:gemini-2.5-flash}") String model,
                                          @Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl) {
    this.webClient = webClientBuilder.baseUrl(removeTrailingSlash(baseUrl)).build();
    this.apiKey = apiKey;
    this.model = model;
  }

  @Override
  public String extractQuery(byte[] imageBytes, String mimeType) {
    if (imageBytes == null || imageBytes.length == 0) {
      throw new ApiValidationException("Ảnh tìm kiếm không hợp lệ.");
    }
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalStateException("Gemini API key chưa được cấu hình.");
    }

    // Tối ưu hóa prompt ép AI đọc text trực tiếp trên nhãn chai để DB dễ match từ khóa
    String prompt = """
      Bạn là hệ thống nhận diện ảnh sản phẩm mỹ phẩm cho website thương mại điện tử.
      Nhiệm vụ:
      - Đọc các văn bản (chữ), thương hiệu và tên sản phẩm in trên bao bì trong ảnh.
      - Tạo ra một cụm từ khóa ngắn gọn bằng tiếng Anh hoặc tiếng Việt chứa [Tên thương hiệu] + [Dòng sản phẩm chính] dựa trên nhãn mác nhìn thấy.
      - CHỈ trả về đúng 1 dòng văn bản thuần duy nhất, không ký tự markdown, không giải thích, không viết hoa toàn bộ.
      - Ví dụ: "iunik beta glucan cream", "dear klairs blue calming toner pad", "la roche posay cicaplast baume b5".
      - Nếu ảnh không rõ hoặc không liên quan mỹ phẩm, trả về: "my pham".
      """;

    Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of(
                    "parts", List.of(
                            Map.of("text", prompt),
                            Map.of("inlineData", Map.of(
                                    "mimeType", mimeType,
                                    "data", Base64.getEncoder().encodeToString(imageBytes)
                            ))
                    )
            ))
    );

    try {
      JsonNode response = webClient.post()
              .uri(uriBuilder -> uriBuilder
                      .path("/models/{model}:generateContent")
                      .queryParam("key", apiKey)
                      .build(model))
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
              .bodyValue(requestBody)
              .retrieve()
              .bodyToMono(JsonNode.class)
              .block(REQUEST_TIMEOUT);

      String text = extractCandidateText(response);
      if (text == null || text.isBlank()) {
        throw new ApiValidationException("Không thể nhận diện nội dung từ hình ảnh này.");
      }
      return text.trim();

    } catch (WebClientResponseException.ServiceUnavailable e) {
      throw new ApiValidationException("Cổng phân tích ảnh AI của Google hiện đang bận (503). Sếp thử lại sau vài giây nhé!");
    } catch (WebClientResponseException e) {
      throw new ApiValidationException("Sự cố kết nối AI (" + e.getStatusCode().value() + "): " + e.getStatusText());
    } catch (Exception e) {
      throw new ApiValidationException("Hệ thống mất quá nhiều thời gian để phân tích ảnh (Timeout). Vui lòng thử lại.");
    }
  }

  private static String extractCandidateText(JsonNode response) {
    if (response == null) {
      return null;
    }
    JsonNode candidates = response.path("candidates");
    if (!candidates.isArray() || candidates.isEmpty()) {
      return null;
    }
    return candidates.path(0)
            .path("content")
            .path("parts")
            .path(0)
            .path("text")
            .asText(null);
  }

  private static String removeTrailingSlash(String value) {
    if (value == null || value.isBlank()) {
      return "https://generativelanguage.googleapis.com/v1beta";
    }
    String trimmed = value.trim();
    return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
  }
}