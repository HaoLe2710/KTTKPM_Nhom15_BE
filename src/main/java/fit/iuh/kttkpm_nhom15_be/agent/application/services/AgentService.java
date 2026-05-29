package fit.iuh.kttkpm_nhom15_be.agent.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.kttkpm_nhom15_be.agent.application.dto.AgentDecision;
import fit.iuh.kttkpm_nhom15_be.agent.application.dto.AgentResult;
import fit.iuh.kttkpm_nhom15_be.agent.domain.models.AgentAction;
import fit.iuh.kttkpm_nhom15_be.ai.GeminiService;
import fit.iuh.kttkpm_nhom15_be.carts.application.dto.AddToCartResult;
import fit.iuh.kttkpm_nhom15_be.carts.application.services.CartService;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.ProductResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.ProductResponse.ProductVariantResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.services.ProductService;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

  private static final String GENERIC_ERROR_REPLY =
    "Xin lỗi, hiện tại mình chưa xử lý được yêu cầu này. Bạn có thể nói rõ hơn tên sản phẩm hoặc nhu cầu của bạn không?";

  private final GeminiService geminiService;
  private final ProductService productService;
  private final CartService cartService;
  private final ObjectMapper objectMapper;

  public AgentResult handleMessage(String userId, String message) {
    String userMessage = trimToNull(message);
    if (userMessage == null) {
      return new AgentResult(
        "Bạn gửi giúp mình nhu cầu hoặc tên sản phẩm cần tư vấn nhé.",
        null,
        Map.of(),
        errorResult("User message rỗng.")
      );
    }

    log.info("USER_MESSAGE={}", userMessage);

    AgentDecision decision;
    try {
      decision = geminiService.decideAction(userMessage);
    } catch (RuntimeException exception) {
      log.warn("Agent could not get a decision from Gemini", exception);
      return new AgentResult(
        GENERIC_ERROR_REPLY,
        null,
        Map.of(),
        errorResult("Không phân tích được yêu cầu người dùng.")
      );
    }

    Map<String, Object> parameters = safeParameters(decision);
    AgentAction action = parseAction(decision.getAction());
    log.info("PARSED_ACTION={}", action != null ? action.name() : decision.getAction());
    log.info("PARSED_PARAMETERS={}", parameters);

    Object toolResult;
    if (action == null) {
      toolResult = errorResult("Không xác định được action phù hợp.");
    } else {
      try {
        toolResult = switch (action) {
          case SEARCH_PRODUCTS -> handleSearchProducts(parameters);
          case GET_PRODUCT_DETAIL -> handleGetProductDetail(parameters);
          case ADD_TO_CART -> handleAddToCart(userId, parameters);
        };
      } catch (RuntimeException exception) {
        log.warn("Agent tool execution failed for action={}", action, exception);
        toolResult = errorResult(friendlyToolError(exception));
      }
    }

    log.info("TOOL_RESULT={}", toDebugJson(toolResult));

    String actionName = action != null ? action.name() : String.valueOf(decision.getAction());
    try {
      String finalReply = geminiService.generateFinalAnswer(userMessage, actionName, toolResult);
      log.info("FINAL_REPLY={}", finalReply);
      return new AgentResult(finalReply, actionName, parameters, toolResult);
    } catch (RuntimeException exception) {
      log.warn("Agent could not get final answer from Gemini. Returning deterministic fallback.", exception);
      String fallbackReply = buildFallbackReply(action, toolResult);
      log.info("FINAL_REPLY={}", fallbackReply);
      return new AgentResult(fallbackReply, actionName, parameters, toolResult);
    }
  }

  private Object handleSearchProducts(Map<String, Object> parameters) {
    List<ProductResponse> products = productService.searchProducts(
      getStringParam(parameters, "keyword"),
      getStringParam(parameters, "category"),
      getStringParam(parameters, "skinType"),
      getStringParam(parameters, "brand"),
      getBigDecimalParam(parameters, "maxPrice")
    );

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("success", true);
    result.put("total", products.size());
    result.put("products", products);
    return result;
  }

  private Object handleGetProductDetail(Map<String, Object> parameters) {
    ProductResponse product = productService.getProductDetail(
      getStringParam(parameters, "productId"),
      getStringParam(parameters, "productName")
    );

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("success", true);
    result.put("product", product);
    return result;
  }

  private Object handleAddToCart(String userId, Map<String, Object> parameters) {
    int quantity = getIntegerParam(parameters, "quantity", 1);
    ProductResponse product = productService.findProductForCart(
      getStringParam(parameters, "productId"),
      getStringParam(parameters, "productName")
    );
    ProductVariantResponse variant = chooseVariant(product, quantity);
    return cartService.addToCart(userId, product, variant, quantity);
  }

  private ProductVariantResponse chooseVariant(ProductResponse product, int quantity) {
    int resolvedQuantity = quantity <= 0 ? 1 : quantity;
    if (product == null || product.variants() == null || product.variants().isEmpty()) {
      return null;
    }
    if (product.defaultVariant() != null
      && product.defaultVariant().active()
      && product.defaultVariant().stockQuantity() >= resolvedQuantity) {
      return product.defaultVariant();
    }
    return product.variants().stream()
      .filter(variant -> variant.active() && variant.stockQuantity() >= resolvedQuantity)
      .min(Comparator.comparing(ProductVariantResponse::price, Comparator.nullsLast(Comparator.naturalOrder())))
      .orElse(null);
  }

  private AgentAction parseAction(String action) {
    String normalized = trimToNull(action);
    if (normalized == null) {
      return null;
    }
    try {
      return AgentAction.valueOf(normalized.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      log.warn("Invalid agent action from Gemini: {}", action);
      return null;
    }
  }

  private Map<String, Object> safeParameters(AgentDecision decision) {
    if (decision == null || decision.getParameters() == null) {
      return Map.of();
    }
    return decision.getParameters();
  }

  private String getStringParam(Map<String, Object> params, String key) {
    Object value = params.get(key);
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return new BigDecimal(number.toString()).stripTrailingZeros().toPlainString();
    }
    String text = trimToNull(String.valueOf(value));
    if (text == null || "null".equalsIgnoreCase(text)) {
      return null;
    }
    return text;
  }

  private BigDecimal getBigDecimalParam(Map<String, Object> params, String key) {
    Object value = params.get(key);
    if (value == null) {
      return null;
    }
    if (value instanceof Number number) {
      return new BigDecimal(number.toString());
    }
    String text = trimToNull(String.valueOf(value));
    if (text == null || "null".equalsIgnoreCase(text)) {
      return null;
    }

    String normalized = text.toLowerCase(Locale.ROOT)
      .replace("vnd", "")
      .replace("vnđ", "")
      .replace("đ", "")
      .replace(" ", "")
      .trim();

    BigDecimal multiplier = BigDecimal.ONE;
    if (normalized.endsWith("k")) {
      multiplier = BigDecimal.valueOf(1000);
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    if (normalized.matches("\\d{1,3}(\\.\\d{3})+")) {
      normalized = normalized.replace(".", "");
    }
    normalized = normalized.replace(",", "");

    try {
      return new BigDecimal(normalized).multiply(multiplier);
    } catch (NumberFormatException exception) {
      log.warn("Could not parse BigDecimal agent parameter {}={}", key, value);
      return null;
    }
  }

  private Long getLongParam(Map<String, Object> params, String key) {
    BigDecimal value = getBigDecimalParam(params, key);
    return value == null ? null : value.longValue();
  }

  private Integer getIntegerParam(Map<String, Object> params, String key, Integer defaultValue) {
    BigDecimal value = getBigDecimalParam(params, key);
    if (value == null) {
      return defaultValue;
    }
    int intValue = value.intValue();
    return intValue <= 0 ? defaultValue : intValue;
  }

  private Map<String, Object> errorResult(String message) {
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("success", false);
    result.put("error", trimToNull(message) == null ? GENERIC_ERROR_REPLY : message);
    return result;
  }

  private String friendlyToolError(RuntimeException exception) {
    if (exception instanceof DataAccessException) {
      return "Hệ thống dữ liệu đang gặp sự cố. Bạn vui lòng thử lại sau ít phút.";
    }
    String message = trimToNull(exception.getMessage());
    return message == null ? GENERIC_ERROR_REPLY : message;
  }

  private String buildFallbackReply(AgentAction action, Object toolResult) {
    String error = extractError(toolResult);
    if (error != null) {
      return error;
    }
    if (toolResult instanceof AddToCartResult addToCartResult && addToCartResult.success()) {
      return "Mình đã thêm " + addToCartResult.quantity() + " sản phẩm " + addToCartResult.productName() + " vào giỏ hàng.";
    }
    if (action == AgentAction.SEARCH_PRODUCTS && toolResult instanceof Map<?, ?> map) {
      Object productsValue = map.get("products");
      if (productsValue instanceof List<?> products && products.isEmpty()) {
        return "Hiện mình chưa tìm thấy sản phẩm phù hợp với yêu cầu của bạn.";
      }
      if (productsValue instanceof List<?> products && !products.isEmpty()) {
        return "Mình tìm được " + products.size() + " sản phẩm phù hợp. Bạn có thể chọn một sản phẩm để xem chi tiết hơn.";
      }
    }
    if (action == AgentAction.GET_PRODUCT_DETAIL && toolResult instanceof Map<?, ?> map) {
      Object productValue = map.get("product");
      if (productValue instanceof ProductResponse product) {
        String stockText = product.totalStock() == null ? "chưa có dữ liệu tồn kho trong kết quả này" : String.valueOf(product.totalStock());
        return "Mình tìm thấy sản phẩm " + product.name() + ". Giá từ " + formatMoney(product.minPrice()) + ", tồn kho hiện tại: " + stockText + ".";
      }
    }
    return GENERIC_ERROR_REPLY;
  }

  private String extractError(Object toolResult) {
    if (toolResult instanceof Map<?, ?> map) {
      Object success = map.get("success");
      Object error = map.get("error");
      if (Boolean.FALSE.equals(success) && error != null) {
        return String.valueOf(error);
      }
    }
    return null;
  }

  private String formatMoney(BigDecimal value) {
    if (value == null) {
      return "chưa có giá";
    }
    return value.stripTrailingZeros().toPlainString() + "đ";
  }

  private String toDebugJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      return String.valueOf(value);
    }
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
