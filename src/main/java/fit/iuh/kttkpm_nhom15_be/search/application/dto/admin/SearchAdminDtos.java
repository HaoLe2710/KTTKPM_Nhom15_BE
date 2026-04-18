package fit.iuh.kttkpm_nhom15_be.search.application.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class SearchAdminDtos {

  private SearchAdminDtos() {
  }

  public record ProjectionEnqueueResponse(
    String productId,
    String status
  ) {
  }

  public record ProjectionTaskResponse(
    String productId,
    String reason,
    String status,
    int attemptCount,
    LocalDateTime nextAttemptAt,
    String lastError,
    LocalDateTime lastAttemptAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
  ) {
  }

  public record ProjectionFailureResponse(
    String productId,
    String eventType,
    String errorMessage,
    int retryCount,
    LocalDateTime failedAt,
    LocalDateTime lastRetriedAt,
    String state,
    String resolutionType,
    String resolutionNote,
    LocalDateTime resolvedAt,
    LocalDateTime updatedAt
  ) {
  }

  public record ProjectionRunResponse(
    String id,
    String runType,
    String status,
    String cursorProductId,
    int processedCount,
    int failedCount,
    LocalDateTime startedAt,
    LocalDateTime finishedAt
  ) {
  }

  public record FailureResolveRequest(
    String resolutionNote
  ) {
  }

  public record SuggestionWriteRequest(
    @NotBlank String keyword,
    @NotBlank String locale,
    @NotNull @Min(-1000) @Max(1000) Integer weight,
    @NotNull Boolean isActive
  ) {
  }

  public record SuggestionResponse(
    String id,
    String keyword,
    String keywordNormalized,
    String locale,
    int weight,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
  ) {
  }

  public record SynonymGroupWriteRequest(
    @NotBlank String code,
    @NotBlank String locale,
    @NotNull Boolean isActive
  ) {
  }

  public record SynonymTermWriteRequest(
    @NotBlank String term
  ) {
  }

  public record SynonymTermResponse(
    String id,
    String term,
    String termNormalized
  ) {
  }

  public record SynonymGroupResponse(
    String id,
    String code,
    String locale,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<SynonymTermResponse> terms
  ) {
  }

  public record TopQueryResponse(
    String queryText,
    String queryNormalized,
    long totalCount
  ) {
  }

  public record ZeroResultQueryResponse(
    String id,
    String queryText,
    String queryNormalized,
    String locale,
    long occurrenceCount,
    LocalDateTime lastSeenAt
  ) {
  }

  public record TopClickedProductResponse(
    String productId,
    String productName,
    long clickCount
  ) {
  }

  public record ProjectionHealthSummaryResponse(
    long activeProducts,
    long projectedProducts,
    long missingProjectionProducts,
    long staleProjectionProducts,
    long queuedTasks,
    long openFailures
  ) {
  }

  public record IndexedFacetValueResponse(
    String facetKey,
    String facetValue,
    String facetLabel
  ) {
  }

  public record SearchPreviewResponse(
    String normalizedProductName,
    String searchableTextSummary,
    List<String> indexedSkus,
    List<IndexedFacetValueResponse> indexedFacetValues,
    int manualBoost,
    BigDecimal averageRating,
    long reviewCount,
    long soldCount
  ) {
  }
}
