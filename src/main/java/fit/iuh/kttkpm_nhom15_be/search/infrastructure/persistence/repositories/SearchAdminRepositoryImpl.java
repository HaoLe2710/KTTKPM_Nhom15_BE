package fit.iuh.kttkpm_nhom15_be.search.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.IndexedFacetValueResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionFailureResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionHealthSummaryResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionRunResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionTaskResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SearchPreviewResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SuggestionResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SynonymGroupResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SynonymTermResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.TopClickedProductResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.TopQueryResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ZeroResultQueryResponse;
import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchAdminRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchAdminRepositoryImpl implements SearchAdminRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public boolean existsProduct(String productId) {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
      SELECT EXISTS(SELECT 1 FROM products WHERE id = :productId)
      """, new MapSqlParameterSource("productId", productId), Boolean.class));
  }

  @Override
  public Page<ProjectionTaskResponse> findProjectionTasks(AdminPageRequest pageRequest) {
    MapSqlParameterSource params = pageParams(pageRequest);
    List<ProjectionTaskResponse> content = jdbcTemplate.query("""
      SELECT product_id, reason, status, attempt_count, next_attempt_at, last_error, last_attempt_at, created_at, updated_at
      FROM search_projection_tasks
      """
      + " ORDER BY " + taskOrderBy(pageRequest)
      + " LIMIT :limit OFFSET :offset", params, (rs, rowNum) -> new ProjectionTaskResponse(
      rs.getString("product_id"),
      rs.getString("reason"),
      rs.getString("status"),
      rs.getInt("attempt_count"),
      toLocalDateTime(rs.getTimestamp("next_attempt_at")),
      rs.getString("last_error"),
      toLocalDateTime(rs.getTimestamp("last_attempt_at")),
      toLocalDateTime(rs.getTimestamp("created_at")),
      toLocalDateTime(rs.getTimestamp("updated_at"))
    ));
    return new PageImpl<>(content, PageRequest.of(pageRequest.page(), pageRequest.size()), count("SELECT COUNT(*) FROM search_projection_tasks"));
  }

  @Override
  public Page<ProjectionFailureResponse> findProjectionFailures(String state, AdminPageRequest pageRequest) {
    StringBuilder where = new StringBuilder();
    MapSqlParameterSource params = pageParams(pageRequest);
    if (state != null && !state.isBlank()) {
      where.append(" WHERE state = :state");
      params.addValue("state", state.trim());
    }

    List<ProjectionFailureResponse> content = jdbcTemplate.query("""
      SELECT product_id, event_type, error_message, retry_count, failed_at, last_retried_at, state, resolution_type, resolution_note, resolved_at, updated_at
      FROM search_projection_failures
      """ + where
      + " ORDER BY " + failureOrderBy(pageRequest)
      + " LIMIT :limit OFFSET :offset", params, (rs, rowNum) -> new ProjectionFailureResponse(
      rs.getString("product_id"),
      rs.getString("event_type"),
      rs.getString("error_message"),
      rs.getInt("retry_count"),
      toLocalDateTime(rs.getTimestamp("failed_at")),
      toLocalDateTime(rs.getTimestamp("last_retried_at")),
      rs.getString("state"),
      rs.getString("resolution_type"),
      rs.getString("resolution_note"),
      toLocalDateTime(rs.getTimestamp("resolved_at")),
      toLocalDateTime(rs.getTimestamp("updated_at"))
    ));

    long total = count("SELECT COUNT(*) FROM search_projection_failures" + where, params);
    return new PageImpl<>(content, PageRequest.of(pageRequest.page(), pageRequest.size()), total);
  }

  @Override
  public Page<ProjectionRunResponse> findProjectionRuns(AdminPageRequest pageRequest) {
    MapSqlParameterSource params = pageParams(pageRequest);
    List<ProjectionRunResponse> content = jdbcTemplate.query("""
      SELECT id, run_type, status, cursor_product_id, processed_count, failed_count, started_at, finished_at
      FROM search_projection_runs
      """
      + " ORDER BY " + runOrderBy(pageRequest)
      + " LIMIT :limit OFFSET :offset", params, (rs, rowNum) -> new ProjectionRunResponse(
      rs.getString("id"),
      rs.getString("run_type"),
      rs.getString("status"),
      rs.getString("cursor_product_id"),
      rs.getInt("processed_count"),
      rs.getInt("failed_count"),
      toLocalDateTime(rs.getTimestamp("started_at")),
      toLocalDateTime(rs.getTimestamp("finished_at")),
      toLocalDateTime(rs.getTimestamp("finished_at")),
      computeDurationMs(rs.getTimestamp("started_at"), rs.getTimestamp("finished_at")),
      null
    ));
    return new PageImpl<>(content, PageRequest.of(pageRequest.page(), pageRequest.size()), count("SELECT COUNT(*) FROM search_projection_runs"));
  }

  @Override
  public Optional<ProjectionFailureResponse> findProjectionFailure(String productId) {
    List<ProjectionFailureResponse> rows = jdbcTemplate.query("""
      SELECT product_id, event_type, error_message, retry_count, failed_at, last_retried_at, state, resolution_type, resolution_note, resolved_at, updated_at
      FROM search_projection_failures
      WHERE product_id = :productId
      ORDER BY failed_at DESC
      LIMIT 1
      """, new MapSqlParameterSource("productId", productId), (rs, rowNum) -> new ProjectionFailureResponse(
      rs.getString("product_id"),
      rs.getString("event_type"),
      rs.getString("error_message"),
      rs.getInt("retry_count"),
      toLocalDateTime(rs.getTimestamp("failed_at")),
      toLocalDateTime(rs.getTimestamp("last_retried_at")),
      rs.getString("state"),
      rs.getString("resolution_type"),
      rs.getString("resolution_note"),
      toLocalDateTime(rs.getTimestamp("resolved_at")),
      toLocalDateTime(rs.getTimestamp("updated_at"))
    ));
    return rows.stream().findFirst();
  }

  @Override
  public void markFailureRetryQueued(String productId) {
    jdbcTemplate.update("""
      WITH target AS (
        SELECT id
        FROM search_projection_failures
        WHERE product_id = :productId
          AND state = 'OPEN'
        ORDER BY failed_at DESC
        LIMIT 1
      )
      UPDATE search_projection_failures
      SET state = 'RETRY_QUEUED',
          last_retried_at = CURRENT_TIMESTAMP,
          updated_at = CURRENT_TIMESTAMP
      WHERE id IN (SELECT id FROM target)
      """, new MapSqlParameterSource("productId", productId));
  }

  @Override
  public void markFailureResolvedManual(String productId, String resolutionNote) {
    jdbcTemplate.update("""
      WITH target AS (
        SELECT id
        FROM search_projection_failures
        WHERE product_id = :productId
          AND state IN ('OPEN', 'RETRY_QUEUED')
        ORDER BY failed_at DESC
        LIMIT 1
      )
      UPDATE search_projection_failures
      SET state = 'RESOLVED_MANUAL',
          resolution_type = 'MANUAL',
          resolution_note = :resolutionNote,
          resolved_at = CURRENT_TIMESTAMP,
          updated_at = CURRENT_TIMESTAMP
      WHERE id IN (SELECT id FROM target)
      """, new MapSqlParameterSource()
      .addValue("productId", productId)
      .addValue("resolutionNote", resolutionNote));
  }

  @Override
  public void clearProjectionTask(String productId) {
    jdbcTemplate.update("DELETE FROM search_projection_tasks WHERE product_id = :productId", new MapSqlParameterSource("productId", productId));
  }

  @Override
  public Page<SuggestionResponse> findSuggestions(String locale, Boolean active, AdminPageRequest pageRequest) {
    StringBuilder where = new StringBuilder(" WHERE 1=1");
    MapSqlParameterSource params = pageParams(pageRequest);
    if (locale != null && !locale.isBlank()) {
      where.append(" AND locale = :locale");
      params.addValue("locale", locale.trim());
    }
    if (active != null) {
      where.append(" AND is_active = :active");
      params.addValue("active", active);
    }

    List<SuggestionResponse> content = jdbcTemplate.query("""
      SELECT id, keyword, keyword_normalized, locale, weight, is_active, created_at, updated_at
      FROM search_suggestions
      """ + where
      + " ORDER BY " + suggestionOrderBy(pageRequest)
      + " LIMIT :limit OFFSET :offset", params, (rs, rowNum) -> new SuggestionResponse(
      rs.getString("id"),
      rs.getString("keyword"),
      rs.getString("keyword_normalized"),
      rs.getString("locale"),
      rs.getInt("weight"),
      rs.getBoolean("is_active"),
      toLocalDateTime(rs.getTimestamp("created_at")),
      toLocalDateTime(rs.getTimestamp("updated_at"))
    ));

    long total = count("SELECT COUNT(*) FROM search_suggestions" + where, params);
    return new PageImpl<>(content, PageRequest.of(pageRequest.page(), pageRequest.size()), total);
  }

  @Override
  public boolean existsSuggestion(String locale, String keywordNormalized, String excludeId) {
    StringBuilder sql = new StringBuilder("""
      SELECT EXISTS(
        SELECT 1
        FROM search_suggestions
        WHERE locale = :locale
          AND keyword_normalized = :keywordNormalized
      """);
    MapSqlParameterSource params = new MapSqlParameterSource()
      .addValue("locale", locale)
      .addValue("keywordNormalized", keywordNormalized);
    if (excludeId != null && !excludeId.isBlank()) {
      sql.append(" AND id <> :excludeId");
      params.addValue("excludeId", excludeId);
    }
    sql.append(")");
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql.toString(), params, Boolean.class));
  }

  @Override
  public String insertSuggestion(String keyword, String keywordNormalized, String locale, int weight, boolean active) {
    String id = UUID.randomUUID().toString();
    jdbcTemplate.update("""
      INSERT INTO search_suggestions (id, keyword, keyword_normalized, locale, weight, is_active, created_at, updated_at)
      VALUES (:id, :keyword, :keywordNormalized, :locale, :weight, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
      """, new MapSqlParameterSource()
      .addValue("id", id)
      .addValue("keyword", keyword)
      .addValue("keywordNormalized", keywordNormalized)
      .addValue("locale", locale)
      .addValue("weight", weight)
      .addValue("active", active));
    return id;
  }

  @Override
  public void updateSuggestion(String id, String keyword, String keywordNormalized, String locale, int weight, boolean active) {
    jdbcTemplate.update("""
      UPDATE search_suggestions
      SET keyword = :keyword,
          keyword_normalized = :keywordNormalized,
          locale = :locale,
          weight = :weight,
          is_active = :active,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = :id
      """, new MapSqlParameterSource()
      .addValue("id", id)
      .addValue("keyword", keyword)
      .addValue("keywordNormalized", keywordNormalized)
      .addValue("locale", locale)
      .addValue("weight", weight)
      .addValue("active", active));
  }

  @Override
  public void updateSuggestionActive(String id, boolean active) {
    jdbcTemplate.update("""
      UPDATE search_suggestions
      SET is_active = :active,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = :id
      """, new MapSqlParameterSource()
      .addValue("id", id)
      .addValue("active", active));
  }

  @Override
  public boolean existsSuggestionId(String id) {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
      SELECT EXISTS(SELECT 1 FROM search_suggestions WHERE id = :id)
      """, new MapSqlParameterSource("id", id), Boolean.class));
  }

  @Override
  public Page<SynonymGroupResponse> findSynonymGroups(String locale, Boolean active, AdminPageRequest pageRequest) {
    StringBuilder where = new StringBuilder(" WHERE 1=1");
    MapSqlParameterSource params = pageParams(pageRequest);
    if (locale != null && !locale.isBlank()) {
      where.append(" AND g.locale = :locale");
      params.addValue("locale", locale.trim());
    }
    if (active != null) {
      where.append(" AND g.is_active = :active");
      params.addValue("active", active);
    }

    List<SynonymGroupResponse> baseGroups = jdbcTemplate.query("""
      SELECT g.id, g.code, g.locale, g.is_active, g.created_at, g.updated_at
      FROM search_synonym_groups g
      """ + where
      + " ORDER BY " + synonymOrderBy(pageRequest)
      + " LIMIT :limit OFFSET :offset", params, (rs, rowNum) -> new SynonymGroupResponse(
      rs.getString("id"),
      rs.getString("code"),
      rs.getString("locale"),
      rs.getBoolean("is_active"),
      toLocalDateTime(rs.getTimestamp("created_at")),
      toLocalDateTime(rs.getTimestamp("updated_at")),
      List.of()
    ));

    Map<String, List<SynonymTermResponse>> termsByGroup = new LinkedHashMap<>();
    if (!baseGroups.isEmpty()) {
      jdbcTemplate.query("""
        SELECT id, synonym_group_id, term, term_normalized
        FROM search_synonym_terms
        WHERE synonym_group_id IN (:groupIds)
        ORDER BY term ASC
        """, new MapSqlParameterSource("groupIds", baseGroups.stream().map(SynonymGroupResponse::id).toList()), rs -> {
        String groupId = rs.getString("synonym_group_id");
        List<SynonymTermResponse> terms = termsByGroup.get(groupId);
        if (terms == null) {
          terms = new ArrayList<>();
          termsByGroup.put(groupId, terms);
        }
        terms.add(new SynonymTermResponse(
          rs.getString("id"),
          rs.getString("term"),
          rs.getString("term_normalized")
        ));
      });
    }

    List<SynonymGroupResponse> content = baseGroups.stream()
      .map(group -> new SynonymGroupResponse(
        group.id(),
        group.code(),
        group.locale(),
        group.active(),
        group.createdAt(),
        group.updatedAt(),
        List.copyOf(termsByGroup.getOrDefault(group.id(), List.of()))
      ))
      .toList();

    long total = count("SELECT COUNT(*) FROM search_synonym_groups g" + where, params);
    return new PageImpl<>(content, PageRequest.of(pageRequest.page(), pageRequest.size()), total);
  }

  @Override
  public boolean existsSynonymGroup(String id) {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
      SELECT EXISTS(SELECT 1 FROM search_synonym_groups WHERE id = :id)
      """, new MapSqlParameterSource("id", id), Boolean.class));
  }

  @Override
  public boolean existsSynonymGroupCode(String locale, String code, String excludeId) {
    StringBuilder sql = new StringBuilder("""
      SELECT EXISTS(
        SELECT 1
        FROM search_synonym_groups
        WHERE locale = :locale
          AND LOWER(code) = LOWER(:code)
      """);
    MapSqlParameterSource params = new MapSqlParameterSource()
      .addValue("locale", locale)
      .addValue("code", code);
    if (excludeId != null && !excludeId.isBlank()) {
      sql.append(" AND id <> :excludeId");
      params.addValue("excludeId", excludeId);
    }
    sql.append(")");
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql.toString(), params, Boolean.class));
  }

  @Override
  public long countSynonymTerms(String groupId) {
    return count("""
      SELECT COUNT(*)
      FROM search_synonym_terms
      WHERE synonym_group_id = :groupId
      """, new MapSqlParameterSource("groupId", groupId));
  }

  @Override
  public String insertSynonymGroup(String code, String locale, boolean active) {
    String id = UUID.randomUUID().toString();
    jdbcTemplate.update("""
      INSERT INTO search_synonym_groups (id, code, locale, is_active, created_at, updated_at)
      VALUES (:id, :code, :locale, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
      """, new MapSqlParameterSource()
      .addValue("id", id)
      .addValue("code", code)
      .addValue("locale", locale)
      .addValue("active", active));
    return id;
  }

  @Override
  public void updateSynonymGroup(String id, String code, String locale, boolean active) {
    jdbcTemplate.update("""
      UPDATE search_synonym_groups
      SET code = :code,
          locale = :locale,
          is_active = :active,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = :id
      """, new MapSqlParameterSource()
      .addValue("id", id)
      .addValue("code", code)
      .addValue("locale", locale)
      .addValue("active", active));
  }

  @Override
  public void updateSynonymGroupActive(String id, boolean active) {
    jdbcTemplate.update("""
      UPDATE search_synonym_groups
      SET is_active = :active,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = :id
      """, new MapSqlParameterSource()
      .addValue("id", id)
      .addValue("active", active));
  }

  @Override
  public String insertSynonymTerm(String groupId, String term, String normalizedTerm) {
    String id = UUID.randomUUID().toString();
    jdbcTemplate.update("""
      INSERT INTO search_synonym_terms (id, synonym_group_id, term, term_normalized)
      VALUES (:id, :groupId, :term, :normalizedTerm)
      """, new MapSqlParameterSource()
      .addValue("id", id)
      .addValue("groupId", groupId)
      .addValue("term", term)
      .addValue("normalizedTerm", normalizedTerm));
    return id;
  }

  @Override
  public boolean existsSynonymTerm(String groupId, String normalizedTerm, String excludeId) {
    StringBuilder sql = new StringBuilder("""
      SELECT EXISTS(
        SELECT 1
        FROM search_synonym_terms
        WHERE synonym_group_id = :groupId
          AND term_normalized = :normalizedTerm
      """);
    MapSqlParameterSource params = new MapSqlParameterSource()
      .addValue("groupId", groupId)
      .addValue("normalizedTerm", normalizedTerm);
    if (excludeId != null && !excludeId.isBlank()) {
      sql.append(" AND id <> :excludeId");
      params.addValue("excludeId", excludeId);
    }
    sql.append(")");
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql.toString(), params, Boolean.class));
  }

  @Override
  public boolean existsSynonymTermId(String termId) {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
      SELECT EXISTS(SELECT 1 FROM search_synonym_terms WHERE id = :id)
      """, new MapSqlParameterSource("id", termId), Boolean.class));
  }

  @Override
  public void deleteSynonymTerm(String termId) {
    jdbcTemplate.update("DELETE FROM search_synonym_terms WHERE id = :id", new MapSqlParameterSource("id", termId));
  }

  @Override
  public List<TopQueryResponse> findTopQueries(LocalDateTime from, LocalDateTime to, String locale, int limit) {
    StringBuilder sql = new StringBuilder("""
      SELECT COALESCE(MAX(query_text), query_normalized) AS query_text,
             query_normalized,
             COUNT(*) AS total_count
      FROM search_query_logs
      WHERE created_at BETWEEN :from AND :to
        AND query_normalized IS NOT NULL
        AND query_normalized <> ''
      """);
    MapSqlParameterSource params = new MapSqlParameterSource()
      .addValue("from", Timestamp.valueOf(from))
      .addValue("to", Timestamp.valueOf(to))
      .addValue("limit", limit);
    if (locale != null && !locale.isBlank()) {
      sql.append(" AND locale = :locale");
      params.addValue("locale", locale.trim());
    }
    sql.append("""
      GROUP BY query_normalized
      ORDER BY total_count DESC, query_normalized ASC
      LIMIT :limit
      """);
    return jdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> new TopQueryResponse(
      rs.getString("query_text"),
      rs.getString("query_normalized"),
      rs.getLong("total_count"),
      null
    ));
  }

  @Override
  public Page<ZeroResultQueryResponse> findZeroResultQueries(LocalDateTime from, LocalDateTime to, String locale, AdminPageRequest pageRequest) {
    StringBuilder where = new StringBuilder(" WHERE last_seen_at BETWEEN :from AND :to");
    MapSqlParameterSource params = pageParams(pageRequest)
      .addValue("from", Timestamp.valueOf(from))
      .addValue("to", Timestamp.valueOf(to));
    if (locale != null && !locale.isBlank()) {
      where.append(" AND locale = :locale");
      params.addValue("locale", locale.trim());
    }

    String sql = """
      SELECT id, query_text, query_normalized, locale, occurrence_count, last_seen_at
      FROM search_zero_result_queries
      """
      + "\n" + where + "\n"
      + "ORDER BY " + zeroResultOrderBy(pageRequest) + "\n"
      + "LIMIT :limit OFFSET :offset";

    List<ZeroResultQueryResponse> content = jdbcTemplate.query(sql, params, (rs, rowNum) -> new ZeroResultQueryResponse(
      rs.getString("id"),
      rs.getString("query_text"),
      rs.getString("query_normalized"),
      rs.getString("locale"),
      rs.getLong("occurrence_count"),
      toLocalDateTime(rs.getTimestamp("last_seen_at")),
      "NO_MATCH",
      false
    ));

    long total = count("SELECT COUNT(*) FROM search_zero_result_queries" + where, params);
    return new PageImpl<>(content, PageRequest.of(pageRequest.page(), pageRequest.size()), total);
  }

  @Override
  public List<TopClickedProductResponse> findTopClickedProducts(LocalDateTime from, LocalDateTime to, String locale, int limit) {
    StringBuilder sql = new StringBuilder("""
      SELECT c.product_id,
             COALESCE(MAX(d.product_name), MAX(p.name)) AS product_name,
             MIN(d.min_price) AS min_price,
             (
               SELECT m.url
               FROM media m
               WHERE m.product_id = c.product_id
               ORDER BY m.is_primary DESC, m.created_at ASC
               LIMIT 1
             ) AS thumbnail_url,
             COUNT(*) AS click_count
      FROM search_click_logs c
      JOIN search_query_logs q ON q.id = c.query_log_id
      LEFT JOIN product_search_documents d ON d.product_id = c.product_id
      LEFT JOIN products p ON p.id = c.product_id
      WHERE c.created_at BETWEEN :from AND :to
      """);
    MapSqlParameterSource params = new MapSqlParameterSource()
      .addValue("from", Timestamp.valueOf(from))
      .addValue("to", Timestamp.valueOf(to))
      .addValue("limit", limit);
    if (locale != null && !locale.isBlank()) {
      sql.append(" AND q.locale = :locale");
      params.addValue("locale", locale.trim());
    }
    sql.append("""
      GROUP BY c.product_id
      ORDER BY click_count DESC, product_name ASC, c.product_id ASC
      LIMIT :limit
      """);

    return jdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> new TopClickedProductResponse(
      rs.getString("product_id"),
      rs.getString("product_name"),
      rs.getLong("click_count"),
      rs.getBigDecimal("min_price"),
      rs.getString("thumbnail_url")
    ));
  }

  @Override
  public ProjectionHealthSummaryResponse getProjectionHealthSummary() {
    long activeProducts = count("""
      SELECT COUNT(*)
      FROM products
      WHERE is_active = TRUE
      """);
    long projectedProducts = count("""
      SELECT COUNT(*)
      FROM product_search_documents d
      JOIN products p ON p.id = d.product_id
      WHERE p.is_active = TRUE
      """);
    long missingProjectionProducts = count("""
      SELECT COUNT(*)
      FROM products p
      LEFT JOIN product_search_documents d ON d.product_id = p.id
      WHERE p.is_active = TRUE
        AND d.product_id IS NULL
      """);
    long staleProjectionProducts = count("""
      SELECT COUNT(*)
      FROM products p
      JOIN product_search_documents d ON d.product_id = p.id
      WHERE p.is_active = TRUE
        AND d.source_updated_at < """ + sourceUpdatedAtSql("p") + """
      """);
    long queuedTasks = count("""
      SELECT COUNT(*)
      FROM search_projection_tasks
      """);
    long openFailures = count("""
      SELECT COUNT(*)
      FROM search_projection_failures
      WHERE state IN ('OPEN', 'RETRY_QUEUED')
      """);
    Double avgLatencyMs = findAverageSearchLatencyMs(LocalDateTime.now().minusDays(7), LocalDateTime.now());
    LocalDateTime lastSyncAt = findLatestProjectionSyncAt();
    double cacheEfficiency = activeProducts == 0 ? 0.0 : (projectedProducts * 100.0) / activeProducts;
    return new ProjectionHealthSummaryResponse(
      activeProducts,
      projectedProducts,
      missingProjectionProducts,
      staleProjectionProducts,
      queuedTasks,
      openFailures,
      avgLatencyMs,
      BigDecimal.valueOf(cacheEfficiency).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue(),
      lastSyncAt
    );
  }

  @Override
  public Double findAverageSearchLatencyMs(LocalDateTime from, LocalDateTime to) {
    BigDecimal value = jdbcTemplate.queryForObject("""
      SELECT AVG(total_latency_ms)::numeric
      FROM search_query_logs
      WHERE created_at BETWEEN :from AND :to
        AND total_latency_ms IS NOT NULL
      """, new MapSqlParameterSource()
      .addValue("from", Timestamp.valueOf(from))
      .addValue("to", Timestamp.valueOf(to)), BigDecimal.class);
    return value == null ? null : value.doubleValue();
  }

  @Override
  public LocalDateTime findLatestProjectionSyncAt() {
    Timestamp ts = jdbcTemplate.queryForObject("""
      SELECT MAX(projection_updated_at)
      FROM product_search_documents
      """, new MapSqlParameterSource(), Timestamp.class);
    return ts == null ? null : ts.toLocalDateTime();
  }

  @Override
  public Optional<SearchPreviewResponse> findSearchPreview(String productId) {
    try {
      SearchPreviewResponse base = jdbcTemplate.queryForObject("""
        SELECT product_name_normalized,
               document_text,
               manual_boost,
               average_rating,
               review_count,
               sold_count
        FROM product_search_documents
        WHERE product_id = :productId
        """, new MapSqlParameterSource("productId", productId), (rs, rowNum) -> new SearchPreviewResponse(
        rs.getString("product_name_normalized"),
        summarizeText(rs.getString("document_text")),
        List.of(),
        List.of(),
        rs.getInt("manual_boost"),
        rs.getBigDecimal("average_rating"),
        rs.getLong("review_count"),
        rs.getLong("sold_count")
      ));

      List<String> indexedSkus = jdbcTemplate.query("""
        SELECT sku_raw
        FROM product_search_skus
        WHERE product_id = :productId
        ORDER BY sku_raw ASC
        """, new MapSqlParameterSource("productId", productId), (rs, rowNum) -> rs.getString("sku_raw"));

      List<IndexedFacetValueResponse> indexedFacetValues = jdbcTemplate.query("""
        SELECT facet_key, facet_value, facet_label
        FROM product_search_facet_values
        WHERE product_id = :productId
        ORDER BY sort_order ASC, facet_label ASC, facet_value ASC
        """, new MapSqlParameterSource("productId", productId), (rs, rowNum) -> new IndexedFacetValueResponse(
        rs.getString("facet_key"),
        rs.getString("facet_value"),
        rs.getString("facet_label")
      ));

      return Optional.of(new SearchPreviewResponse(
        base.normalizedProductName(),
        base.searchableTextSummary(),
        List.copyOf(indexedSkus),
        List.copyOf(indexedFacetValues),
        base.manualBoost(),
        base.averageRating(),
        base.reviewCount(),
        base.soldCount()
      ));
    } catch (EmptyResultDataAccessException ignored) {
      return Optional.empty();
    }
  }

  private MapSqlParameterSource pageParams(AdminPageRequest pageRequest) {
    return new MapSqlParameterSource()
      .addValue("limit", pageRequest.size())
      .addValue("offset", pageRequest.page() * pageRequest.size());
  }

  private String taskOrderBy(AdminPageRequest pageRequest) {
    return columnFor(pageRequest, Map.of(
      "nextAttemptAt", "next_attempt_at",
      "updatedAt", "updated_at",
      "productId", "product_id",
      "status", "status"
    )) + direction(pageRequest) + ", product_id ASC";
  }

  private String failureOrderBy(AdminPageRequest pageRequest) {
    return columnFor(pageRequest, Map.of(
      "failedAt", "failed_at",
      "retryCount", "retry_count",
      "productId", "product_id"
    )) + direction(pageRequest) + ", product_id ASC";
  }

  private String runOrderBy(AdminPageRequest pageRequest) {
    return columnFor(pageRequest, Map.of(
      "startedAt", "started_at",
      "finishedAt", "finished_at",
      "processedCount", "processed_count"
    )) + direction(pageRequest) + ", id ASC";
  }

  private String suggestionOrderBy(AdminPageRequest pageRequest) {
    return columnFor(pageRequest, Map.of(
      "weight", "weight",
      "keyword", "keyword",
      "updatedAt", "updated_at"
    )) + direction(pageRequest) + ", keyword ASC, id ASC";
  }

  private String synonymOrderBy(AdminPageRequest pageRequest) {
    return columnFor(pageRequest, Map.of(
      "updatedAt", "updated_at",
      "code", "code",
      "locale", "locale"
    )) + direction(pageRequest) + ", code ASC, id ASC";
  }

  private String zeroResultOrderBy(AdminPageRequest pageRequest) {
    return columnFor(pageRequest, Map.of(
      "occurrenceCount", "occurrence_count",
      "lastSeenAt", "last_seen_at"
    )) + direction(pageRequest) + ", id ASC";
  }

  private String columnFor(AdminPageRequest pageRequest, Map<String, String> columns) {
    return columns.getOrDefault(pageRequest.sortField(), columns.values().iterator().next());
  }

  private String direction(AdminPageRequest pageRequest) {
    return pageRequest.sortDirection() == AdminPageRequest.SortDirection.DESC ? " DESC" : " ASC";
  }

  private String sourceUpdatedAtSql(String productAlias) {
    return "GREATEST("
      + "COALESCE(" + productAlias + ".updated_at, " + productAlias + ".created_at), "
      + "COALESCE((SELECT MAX(v.updated_at) FROM variants v WHERE v.product_id = " + productAlias + ".id), " + productAlias + ".created_at), "
      + "COALESCE((SELECT MAX(r.created_at) FROM reviews r WHERE r.product_id = " + productAlias + ".id), " + productAlias + ".created_at), "
      + "COALESCE(("
      + "SELECT MAX(o.updated_at) "
      + "FROM orders o "
      + "JOIN order_items oi ON oi.order_id = o.id "
      + "JOIN variants v2 ON v2.id = oi.variant_id "
      + "WHERE v2.product_id = " + productAlias + ".id"
      + "), " + productAlias + ".created_at), "
      + "COALESCE(("
      + "SELECT MAX(spbo.updated_at) "
      + "FROM search_product_boost_overrides spbo "
      + "WHERE spbo.product_id = " + productAlias + ".id"
      + "), " + productAlias + ".created_at)"
      + ")";
  }

  private long count(String sql) {
    return count(sql, new MapSqlParameterSource());
  }

  private long count(String sql, MapSqlParameterSource params) {
    Long result = jdbcTemplate.queryForObject(sql, params, Long.class);
    return result == null ? 0L : result;
  }

  private LocalDateTime toLocalDateTime(Timestamp timestamp) {
    return timestamp == null ? null : timestamp.toLocalDateTime();
  }

  private String summarizeText(String text) {
    if (text == null || text.isBlank()) {
      return "";
    }
    return text.length() <= 500 ? text : text.substring(0, 500);
  }

  private Long computeDurationMs(Timestamp startedAt, Timestamp finishedAt) {
    if (startedAt == null || finishedAt == null) {
      return null;
    }
    return finishedAt.getTime() - startedAt.getTime();
  }
}
