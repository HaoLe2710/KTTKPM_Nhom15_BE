package fit.iuh.kttkpm_nhom15_be.search.application.support;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class SearchNormalizer {

  private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
  private static final Pattern NON_TEXT = Pattern.compile("[^\\p{IsAlphabetic}\\p{IsDigit}]+");
  private static final Pattern NON_SKU = Pattern.compile("[^A-Z0-9]+");
  private static final Pattern WHITESPACE = Pattern.compile("\\s+");

  private SearchNormalizer() {}

  public static String normalizeText(String input) {
    if (input == null || input.isBlank()) {
      return "";
    }

    String normalized = Normalizer.normalize(input, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT).trim();
    normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
    normalized = DIACRITICS.matcher(normalized).replaceAll("");
    normalized = NON_TEXT.matcher(normalized).replaceAll(" ");
    normalized = WHITESPACE.matcher(normalized).replaceAll(" ").trim();
    return normalized;
  }

  public static String normalizeSku(String input) {
    if (input == null || input.isBlank()) {
      return "";
    }

    String normalized = Normalizer.normalize(input, Normalizer.Form.NFKC)
      .toUpperCase(Locale.ROOT)
      .trim();
    return NON_SKU.matcher(normalized).replaceAll("");
  }

  public static List<String> tokenize(String input) {
    String normalized = normalizeText(input);
    if (normalized.isBlank()) {
      return List.of();
    }
    return Arrays.stream(normalized.split(" "))
      .filter(Objects::nonNull)
      .map(String::trim)
      .filter(token -> !token.isEmpty())
      .distinct()
      .toList();
  }
}
