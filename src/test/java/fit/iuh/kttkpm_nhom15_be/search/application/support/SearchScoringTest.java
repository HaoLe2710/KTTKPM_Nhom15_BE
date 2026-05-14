package fit.iuh.kttkpm_nhom15_be.search.application.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SearchScoringTest {

  @Test
  void facetAlignmentScoreUsesLockedWeightsAndCap() {
    int score = SearchScoring.facetAlignmentScore(true, true, true, true, true, true, true, true, true);
    assertEquals(30, score);
  }

  @Test
  void popularityScoreUsesSoldCountReviewCountAndRatingOnly() {
    double score = SearchScoring.popularityScore(25, new BigDecimal("4.50"), 10);
    assertTrue(score > 0);
    assertTrue(score <= 35);
  }

  @Test
  void merchandisingScoreCapsAtSixty() {
    assertEquals(60, SearchScoring.merchandisingScore(50, true, true, true, 20));
  }
}
