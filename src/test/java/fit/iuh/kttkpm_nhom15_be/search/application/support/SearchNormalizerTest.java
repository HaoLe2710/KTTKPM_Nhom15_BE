package fit.iuh.kttkpm_nhom15_be.search.application.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SearchNormalizerTest {

  @Test
  void normalizeTextLowercasesStripsDiacriticsAndCollapsesWhitespace() {
    assertEquals("sua rua mat", SearchNormalizer.normalizeText("  Sữa   Rửa-Mặt  "));
  }

  @Test
  void normalizeSkuRemovesSeparatorsAndKeepsOnlyAlphanumeric() {
    assertEquals("SKU123ABC", SearchNormalizer.normalizeSku(" sku-123/abc "));
  }
}
