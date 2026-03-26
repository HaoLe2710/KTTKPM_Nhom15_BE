package fit.iuh.kttkpm_nhom15_be.search.application.support;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class SearchScoring {

  private SearchScoring() {}

  public static int facetAlignmentScore(
    boolean brandHit,
    boolean typeHit,
    boolean ingredientHit,
    boolean benefitHit,
    boolean concernHit,
    boolean skinTypeHit,
    boolean tagHit,
    boolean collectionHit,
    boolean attributeHit
  ) {
    int score = 0;
    score += brandHit ? 12 : 0;
    score += typeHit ? 10 : 0;
    score += ingredientHit ? 8 : 0;
    score += benefitHit ? 8 : 0;
    score += concernHit ? 8 : 0;
    score += skinTypeHit ? 8 : 0;
    score += tagHit ? 5 : 0;
    score += collectionHit ? 5 : 0;
    score += attributeHit ? 4 : 0;
    return Math.min(30, score);
  }

  public static double popularityScore(int soldCount, BigDecimal averageRating, int reviewCount) {
    double rating = averageRating == null ? 0.0 : averageRating.doubleValue();
    double score = 8 * Math.log1p(Math.max(0, soldCount))
      + 10 * ((rating / 5.0) * Math.log1p(Math.max(0, reviewCount)));
    return Math.min(35.0, score);
  }

  public static int merchandisingScore(int manualBoost, boolean featured, boolean isNew, boolean bestSeller, int boostRuleScore) {
    int score = manualBoost + boostRuleScore;
    score += featured ? 5 : 0;
    score += isNew ? 5 : 0;
    score += bestSeller ? 10 : 0;
    return Math.min(60, score);
  }

  public static BigDecimal roundScore(double value) {
    return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);
  }
}
