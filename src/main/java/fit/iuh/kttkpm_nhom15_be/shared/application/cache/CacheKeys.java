package fit.iuh.kttkpm_nhom15_be.shared.application.cache;

import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public final class CacheKeys {

  private static final String NULL_VALUE = "~";

  private CacheKeys() {
  }

  public static String productDetail(String productId) {
    return "productId=" + value(productId);
  }

  public static String productList(String typeId, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
    return String.join("|",
      "typeId=" + value(typeId),
      "minPrice=" + decimal(minPrice),
      "maxPrice=" + decimal(maxPrice),
      "page=" + page,
      "size=" + size
    );
  }

  public static String productSearch(String query,
                                     List<String> typeIds,
                                     BigDecimal minPrice,
                                     BigDecimal maxPrice,
                                     Boolean inStock,
                                     String sort,
                                     int page,
                                     int size) {
    return String.join("|",
      "q=" + rawValue(query),
      "typeIds=" + values(typeIds),
      "minPrice=" + decimal(minPrice),
      "maxPrice=" + decimal(maxPrice),
      "inStock=" + value(inStock),
      "sort=" + value(sort),
      "page=" + page,
      "size=" + size
    );
  }

  public static String searchSuggestions(String query) {
    String normalized = query == null ? null : query.trim().toLowerCase(Locale.ROOT);
    return "q=" + value(normalized);
  }

  public static String masterData(String group) {
    return "group=" + value(group);
  }

  public static String adminMasterData(String group, Boolean active, AdminPageRequest pageRequest) {
    return String.join("|",
      "group=" + value(group),
      "active=" + value(active),
      "page=" + pageRequest.page(),
      "size=" + pageRequest.size(),
      "sort=" + value(pageRequest.sortField()),
      "direction=" + pageRequest.sortDirection().name().toLowerCase(Locale.ROOT)
    );
  }

  private static String values(List<String> input) {
    if (input == null || input.isEmpty()) {
      return NULL_VALUE;
    }
    return input.stream()
      .filter(item -> item != null && !item.isBlank())
      .map(String::trim)
      .sorted()
      .map(CacheKeys::value)
      .reduce((left, right) -> left + "," + right)
      .orElse(NULL_VALUE);
  }

  private static String decimal(BigDecimal input) {
    return input == null ? NULL_VALUE : input.stripTrailingZeros().toPlainString();
  }

  private static String value(Boolean input) {
    return input == null ? NULL_VALUE : input.toString();
  }

  private static String value(String input) {
    if (input == null) {
      return NULL_VALUE;
    }
    String trimmed = input.trim();
    if (trimmed.isEmpty()) {
      return NULL_VALUE;
    }
    return URLEncoder.encode(trimmed, StandardCharsets.UTF_8).replace("+", "%20");
  }

  private static String rawValue(String input) {
    if (input == null || input.isEmpty()) {
      return NULL_VALUE;
    }
    return URLEncoder.encode(input, StandardCharsets.UTF_8).replace("+", "%20");
  }
}
