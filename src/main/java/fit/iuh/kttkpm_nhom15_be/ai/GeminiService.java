package fit.iuh.kttkpm_nhom15_be.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.kttkpm_nhom15_be.agent.application.dto.AgentDecision;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class GeminiService {

  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

  private final WebClient webClient;
  private final ObjectMapper objectMapper;
  private final String apiKey;
  private final String model;

  public GeminiService(
    ObjectMapper objectMapper,
    WebClient.Builder webClientBuilder,
    @Value("${gemini.api.key:}") String apiKey,
    @Value("${gemini.model:gemini-2.5-flash}") String model,
    @Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl
  ) {
    this.objectMapper = objectMapper;
    this.apiKey = apiKey;
    this.model = model;
    this.webClient = webClientBuilder.baseUrl(removeTrailingSlash(baseUrl)).build();
  }

  public AgentDecision decideAction(String userMessage) {
    String prompt = buildDecisionPrompt(userMessage);
    String rawResponse = generateContent(prompt);
    log.info("GEMINI_RAW_DECISION={}", rawResponse);

    try {
      AgentDecision decision = objectMapper.readValue(cleanJson(rawResponse), AgentDecision.class);
      if (decision.getParameters() == null) {
        decision.setParameters(new HashMap<>());
      }
      return decision;
    } catch (JsonProcessingException exception) {
      log.warn("Gemini returned invalid decision JSON. raw={}", rawResponse, exception);
      throw new ApiValidationException("Gemini trả về định dạng phân tích không hợp lệ.");
    }
  }

  public String generateFinalAnswer(String userMessage, String action, Object toolResult) {
    String toolResultJson = toJson(toolResult);
    String prompt = buildFinalAnswerPrompt(userMessage, action, toolResultJson);
    return generateContent(prompt).trim();
  }

  public String generateContent(String prompt) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalStateException("Gemini API key chưa được cấu hình. Vui lòng đặt biến môi trường GEMINI_API_KEY.");
    }

    Map<String, Object> requestBody = Map.of(
      "contents", List.of(Map.of(
        "parts", List.of(Map.of("text", prompt))
      ))
    );

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
      throw new IllegalStateException("Gemini không trả về nội dung phản hồi.");
    }
    return text;
  }

  private String extractCandidateText(JsonNode response) {
    if (response == null) {
      return null;
    }
    JsonNode candidates = response.path("candidates");
    if (!candidates.isArray() || candidates.isEmpty()) {
      log.warn("Gemini response has no candidates. response={}", response);
      return null;
    }
    return candidates.path(0)
      .path("content")
      .path("parts")
      .path(0)
      .path("text")
      .asText(null);
  }

  private String cleanJson(String text) {
    if (text == null) {
      return "";
    }
    return text
      .replace("```json", "")
      .replace("```", "")
      .trim();
  }

  private String buildDecisionPrompt(String userMessage) {
    return """
      Bạn là AI Agent điều phối cho website bán mỹ phẩm.

      Nhiệm vụ của bạn là đọc câu hỏi người dùng và chọn đúng một action phù hợp.

      Các action hợp lệ:

      1. SEARCH_PRODUCTS
      Dùng khi người dùng muốn tìm kiếm, gợi ý hoặc lọc sản phẩm.
      Parameters:
      - keyword: string hoặc null
      - category: string hoặc null
      - skinType: string hoặc null
      - maxPrice: number hoặc null
      - brand: string hoặc null

      2. GET_PRODUCT_DETAIL
      Dùng khi người dùng muốn xem chi tiết, thành phần, công dụng, cách dùng, tồn kho hoặc thông tin của một sản phẩm cụ thể.
      Parameters:
      - productId: string hoặc null
      - productName: string hoặc null

      3. ADD_TO_CART
      Dùng khi người dùng muốn thêm sản phẩm vào giỏ hàng hoặc muốn mua sản phẩm.
      Parameters:
      - productId: string hoặc null
      - productName: string hoặc null
      - quantity: number, mặc định là 1 nếu người dùng không nói rõ

      Quy tắc bắt buộc:
      - Chỉ trả về JSON hợp lệ.
      - Không giải thích thêm.
      - Không thêm markdown.
      - Không bọc JSON trong ```json.
      - Không bịa thông tin.
      - Nếu thiếu thông tin thì vẫn chọn action gần đúng nhất và để field chưa biết là null.
      - Nếu người dùng nói giá kiểu "300k" thì hiểu là 300000.
      - Nếu người dùng không nói số lượng khi ADD_TO_CART thì quantity = 1.

      Format output bắt buộc:

      {
        "action": "SEARCH_PRODUCTS | GET_PRODUCT_DETAIL | ADD_TO_CART",
        "parameters": {}
      }

      Câu hỏi người dùng:
      "{message}"
      """.replace("{message}", userMessage == null ? "" : userMessage);
  }

  private String buildFinalAnswerPrompt(String userMessage, String action, String toolResultJson) {
    return """
      Bạn là AI tư vấn bán mỹ phẩm cho website thương mại điện tử.

      Nguyên tắc:
      - Trả lời bằng tiếng Việt.
      - Thân thiện, tự nhiên, dễ hiểu.
      - Chỉ dựa vào dữ liệu hệ thống cung cấp.
      - Không bịa tên sản phẩm, giá, tồn kho, công dụng.
      - Không chẩn đoán bệnh da liễu.
      - Không cam kết sản phẩm chữa khỏi mụn, nám, dị ứng hoặc bệnh da.
      - Nếu người dùng có dấu hiệu dị ứng, viêm da nặng, mụn viêm kéo dài, hãy khuyên họ gặp bác sĩ da liễu.
      - Nếu không có dữ liệu phù hợp, hãy nói rõ là hiện chưa tìm thấy sản phẩm phù hợp.
      - Nếu action là ADD_TO_CART thành công, hãy xác nhận sản phẩm đã được thêm vào giỏ.
      - Nếu action là ADD_TO_CART thất bại, hãy giải thích lý do ngắn gọn.

      Câu hỏi người dùng:
      {userMessage}

      Action đã thực hiện:
      {action}

      Dữ liệu hệ thống trả về:
      {toolResult}

      Hãy viết câu trả lời cuối cùng cho người dùng.
      """
      .replace("{userMessage}", userMessage == null ? "" : userMessage)
      .replace("{action}", action == null ? "" : action)
      .replace("{toolResult}", toolResultJson == null ? "{}" : toolResultJson);
  }

  private String toJson(Object value) {
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      log.warn("Could not serialize tool result for Gemini final prompt", exception);
      return String.valueOf(value);
    }
  }

  private static String removeTrailingSlash(String value) {
    if (value == null || value.isBlank()) {
      return "https://generativelanguage.googleapis.com/v1beta";
    }
    String trimmed = value.trim();
    return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
  }
}
