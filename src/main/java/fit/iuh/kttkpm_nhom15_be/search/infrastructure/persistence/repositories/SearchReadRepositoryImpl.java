package fit.iuh.kttkpm_nhom15_be.search.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.ProductSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchFacetBucketDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchFacetDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchProductItemDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchProductsRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchRedirectDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchSuggestionDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.models.SearchQueryContext;
import fit.iuh.kttkpm_nhom15_be.search.application.results.SearchPageResult;
import fit.iuh.kttkpm_nhom15_be.search.application.support.SearchNormalizer;
import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchReadRepository;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SearchReadRepositoryImpl implements SearchReadRepository {

  private static final int RELEVANCE_QUERY_LIMIT = 200;
  private static final RowMapper<SearchProductItemDTO> SEARCH_ITEM_ROW_MAPPER = SearchReadRepositoryImpl::mapSearchItem;

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public Page<ProductSummaryDTO> browseLegacy(String typeId, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
    String where = buildLegacyFilters(typeId, minPrice, maxPrice);

    MapSqlParameterSource params = new MapSqlParameterSource()
      .addValue("typeId", typeId)
      .addValue("minPrice", minPrice)
      .addValue("maxPrice", maxPrice)
      .addValue("limit", size)
      .addValue("offset", page * size);

    List<ProductSummaryDTO> items = jdbcTemplate.query("""
      SELECT d.product_id,
             d.type_id,
             d.product_name,
             d.slug,
             d.min_price,
             COALESCE((
                 SELECT SUM(v.stock_quantity)
                 FROM variants v
                 WHERE v.product_id = d.product_id
                   AND v.is_active = TRUE
             ), 0) AS total_initial_stock
      FROM product_search_documents d
      """ + where + """
      ORDER BY d.product_name ASC, d.product_id ASC
      LIMIT :limit OFFSET :offset
      """, params, (rs, rowNum) -> new ProductSummaryDTO(
      rs.getString("product_id"),
      rs.getString("type_id"),
      rs.getString("product_name"),
      rs.getString("slug"),
      rs.getBigDecimal("min_price"),
      rs.getLong("total_initial_stock")
    ));

    Long total = jdbcTemplate.queryForObject("""
      SELECT COUNT(*)
      FROM product_search_documents d
      """ + where, params, Long.class);

    return new PageImpl<>(items, PageRequest.of(page, size), total == null ? 0 : total);
  }

  @Override
  public SearchRedirectDTO findRedirect(String locale, String normalizedQuery) {
    if (normalizedQuery == null || normalizedQuery.isBlank()) {
      return null;
    }

    try {
      return jdbcTemplate.queryForObject("""
        SELECT redirect_type, redirect_target
        FROM search_redirect_rules
        WHERE locale = :locale
          AND is_active = TRUE
          AND exact_match = TRUE
          AND query_pattern_normalized = :query
        ORDER BY updated_at DESC NULLS LAST, created_at DESC
        LIMIT 1
        """,
        new MapSqlParameterSource()
          .addValue("locale", locale)
          .addValue("query", normalizedQuery),
        (rs, rowNum) -> new SearchRedirectDTO(rs.getString("redirect_type"), rs.getString("redirect_target")));
    } catch (EmptyResultDataAccessException ignored) {
      return null;
    }
  }

  @Override
  public List<String> findSynonymTerms(String locale, Collection<String> normalizedTerms) {
    if (normalizedTerms == null || normalizedTerms.isEmpty()) {
      return List.of();
    }

    return jdbcTemplate.query("""
      SELECT DISTINCT t2.term_normalized
      FROM search_synonym_terms t1
      JOIN search_synonym_groups g ON g.id = t1.synonym_group_id
      JOIN search_synonym_terms t2 ON t2.synonym_group_id = g.id
      WHERE g.locale = :locale
        AND g.is_active = TRUE
        AND t1.term_normalized IN (:terms)
      ORDER BY t2.term_normalized
      """,
      new MapSqlParameterSource()
        .addValue("locale", locale)
        .addValue("terms", normalizedTerms),
      (rs, rowNum) -> rs.getString("term_normalized"));
  }

  @Override
  public SearchPageResult search(SearchProductsRequest request, SearchQueryContext queryContext) {
    MapSqlParameterSource params = baseSearchParams(request, queryContext)
      .addValue("limit", Math.max(request.size() * 3, RELEVANCE_QUERY_LIMIT));

    String filters = buildSearchFilters(request);
    String searchCondition = buildSearchCondition(queryContext.normalizedQuery());
    String candidateSql = "SELECT d.product_id, d.slug, d.product_name, d.type_id, d.type_name, "
      + "d.min_price, d.max_price, d.in_stock, d.active_variant_count, d.average_rating, "
      + "d.review_count, d.sold_count, d.source_updated_at, "
      + "GREATEST(CASE WHEN lower(d.slug) = :rawLowerQuery THEN 1000 ELSE 0 END, "
      + "CASE WHEN d.product_name_normalized = :normalizedQuery THEN 900 ELSE 0 END, "
      + "CASE WHEN EXISTS (SELECT 1 FROM product_search_skus s WHERE s.product_id = d.product_id AND s.sku_normalized = :normalizedSku) "
      + "THEN 950 ELSE 0 END) AS exact_match_score, "
      + "(CASE WHEN d.product_name_normalized LIKE :queryLike THEN 300 ELSE 0 END "
      + "+ CASE WHEN d.document_text LIKE :queryLike THEN 220 ELSE 0 END "
      + "+ CASE WHEN COALESCE(d.brand_name_normalized, '') LIKE :queryLike OR COALESCE(lower(d.type_name), '') LIKE :queryLike "
      + "THEN 120 ELSE 0 END) AS phrase_match_score, "
      + "CASE WHEN :hasTsQuery = TRUE THEN ts_rank_cd("
      + "COALESCE(setweight(d.title_vector, 'A'), ''::tsvector) || "
      + "COALESCE(setweight(d.keyword_vector, 'B'), ''::tsvector) || "
      + "COALESCE(setweight(d.body_vector, 'C'), ''::tsvector), "
      + "to_tsquery('simple', :tsQuery)) * 100 ELSE 0 END AS fts_score, "
      + "LEAST(30, "
      + "(CASE WHEN COALESCE(d.brand_name_normalized, '') IN (:expandedTerms) THEN 12 ELSE 0 END) + "
      + "(CASE WHEN COALESCE(lower(d.type_name), '') IN (:expandedTerms) THEN 10 ELSE 0 END) + "
      + "(CASE WHEN EXISTS (SELECT 1 FROM product_search_facet_values f WHERE f.product_id = d.product_id AND f.facet_key = 'ingredient' AND f.facet_value IN (:expandedTerms)) THEN 8 ELSE 0 END) + "
      + "(CASE WHEN EXISTS (SELECT 1 FROM product_search_facet_values f WHERE f.product_id = d.product_id AND f.facet_key = 'benefit' AND f.facet_value IN (:expandedTerms)) THEN 8 ELSE 0 END) + "
      + "(CASE WHEN EXISTS (SELECT 1 FROM product_search_facet_values f WHERE f.product_id = d.product_id AND f.facet_key = 'concern' AND f.facet_value IN (:expandedTerms)) THEN 8 ELSE 0 END) + "
      + "(CASE WHEN EXISTS (SELECT 1 FROM product_search_facet_values f WHERE f.product_id = d.product_id AND f.facet_key = 'skin_type' AND f.facet_value IN (:expandedTerms)) THEN 8 ELSE 0 END) + "
      + "(CASE WHEN EXISTS (SELECT 1 FROM product_search_facet_values f WHERE f.product_id = d.product_id AND f.facet_key = 'tag' AND f.facet_value IN (:expandedTerms)) THEN 5 ELSE 0 END) + "
      + "(CASE WHEN EXISTS (SELECT 1 FROM product_search_facet_values f WHERE f.product_id = d.product_id AND f.facet_key = 'collection' AND f.facet_value IN (:expandedTerms)) THEN 5 ELSE 0 END) + "
      + "(CASE WHEN EXISTS (SELECT 1 FROM product_search_facet_values f WHERE f.product_id = d.product_id AND f.facet_key = 'attribute' AND f.facet_value IN (:expandedTerms)) THEN 4 ELSE 0 END)) AS facet_alignment_score, "
      + "LEAST(35, 8 * LN(1 + d.sold_count) + 10 * ((d.average_rating / 5.0) * LN(1 + d.review_count))) AS popularity_score, "
      + "LEAST(60, d.manual_boost + CASE WHEN d.is_featured THEN 5 ELSE 0 END + CASE WHEN d.is_new THEN 5 ELSE 0 END + "
      + "CASE WHEN d.is_best_seller THEN 10 ELSE 0 END + COALESCE((SELECT SUM(b.boost_value) FROM search_boost_rules b "
      + "WHERE b.locale = :locale AND b.is_active = TRUE AND ((b.exact_match = TRUE AND b.query_pattern_normalized = :normalizedQuery) "
      + "OR (b.exact_match = FALSE AND :normalizedQuery LIKE CONCAT('%%', b.query_pattern_normalized, '%%'))) "
      + "AND (b.product_id IS NULL OR b.product_id = d.product_id) "
      + "AND (b.facet_key IS NULL OR EXISTS (SELECT 1 FROM product_search_facet_values f WHERE f.product_id = d.product_id "
      + "AND f.facet_key = b.facet_key AND f.facet_value = b.facet_value))), 0)) AS merchandising_score, "
      + "CASE WHEN d.in_stock THEN 0 WHEN lower(d.slug) = :rawLowerQuery THEN 0 "
      + "WHEN EXISTS (SELECT 1 FROM product_search_skus s WHERE s.product_id = d.product_id AND s.sku_normalized = :normalizedSku) "
      + "THEN 0 ELSE -25 END AS availability_adjustment "
      + "FROM product_search_documents d WHERE " + filters + " AND " + searchCondition;

    String scoredSql = "SELECT c.*, "
      + "(c.exact_match_score + c.phrase_match_score + c.fts_score + c.facet_alignment_score + "
      + "c.popularity_score + c.merchandising_score + c.availability_adjustment) AS total_score "
      + "FROM (" + candidateSql + ") c "
      + "ORDER BY " + resolveSearchOrder(request) + " "
      + "LIMIT :limit";

    List<SearchProductItemDTO> allCandidates = jdbcTemplate.query(scoredSql, params, SEARCH_ITEM_ROW_MAPPER);

    int fromIndex = Math.min(request.page() * request.size(), allCandidates.size());
    int toIndex = Math.min(fromIndex + request.size(), allCandidates.size());
    List<SearchProductItemDTO> paged = allCandidates.subList(fromIndex, toIndex);

    Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM (" + candidateSql + ") c", params, Long.class);

    return new SearchPageResult(paged, total == null ? 0 : total);
  }

  @Override
  public List<SearchFacetDTO> findFacets(SearchProductsRequest request, SearchQueryContext queryContext) {
    MapSqlParameterSource params = baseSearchParams(request, queryContext);
    String filters = buildSearchFilters(request);
    String searchCondition = buildSearchCondition(queryContext.normalizedQuery());
    String candidateCte = "WITH filtered_products AS ( "
      + "SELECT d.product_id, d.type_id, d.type_name, d.in_stock "
      + "FROM product_search_documents d "
      + "WHERE " + filters + " AND " + searchCondition + " ) ";

    Map<String, List<SearchFacetBucketDTO>> grouped = new LinkedHashMap<>();

    jdbcTemplate.query(candidateCte + """
      SELECT 'type' AS facet_key, fp.type_id AS facet_value, COALESCE(fp.type_name, fp.type_id) AS facet_label, COUNT(*) AS facet_count
      FROM filtered_products fp
      WHERE fp.type_id IS NOT NULL
      GROUP BY fp.type_id, fp.type_name
      ORDER BY facet_count DESC, facet_label ASC
      """, params, (RowCallbackHandler) rs -> grouped.computeIfAbsent("type", ignored -> new ArrayList<>())
      .add(new SearchFacetBucketDTO(rs.getString("facet_value"), rs.getString("facet_label"), rs.getLong("facet_count"))));

    jdbcTemplate.query(candidateCte + """
      SELECT 'stock' AS facet_key,
             CASE WHEN fp.in_stock THEN 'in_stock' ELSE 'out_of_stock' END AS facet_value,
             CASE WHEN fp.in_stock THEN 'In stock' ELSE 'Out of stock' END AS facet_label,
             COUNT(*) AS facet_count
      FROM filtered_products fp
      GROUP BY fp.in_stock
      ORDER BY facet_count DESC, facet_label ASC
      """, params, (RowCallbackHandler) rs -> grouped.computeIfAbsent("stock", ignored -> new ArrayList<>())
      .add(new SearchFacetBucketDTO(rs.getString("facet_value"), rs.getString("facet_label"), rs.getLong("facet_count"))));

    jdbcTemplate.query(candidateCte + """
      SELECT f.facet_key, f.facet_value, f.facet_label, COUNT(*) AS facet_count
      FROM filtered_products fp
      JOIN product_search_facet_values f ON f.product_id = fp.product_id
      GROUP BY f.facet_key, f.facet_value, f.facet_label
      ORDER BY f.facet_key ASC, facet_count DESC, f.facet_label ASC
      """, params, (RowCallbackHandler) rs -> grouped.computeIfAbsent(rs.getString("facet_key"), ignored -> new ArrayList<>())
      .add(new SearchFacetBucketDTO(rs.getString("facet_value"), rs.getString("facet_label"), rs.getLong("facet_count"))));

    return grouped.entrySet().stream()
      .map(entry -> new SearchFacetDTO(entry.getKey(), entry.getValue()))
      .toList();
  }

  @Override
  public List<SearchSuggestionDTO> findSuggestions(String normalizedQuery) {
    if (normalizedQuery == null || normalizedQuery.isBlank()) {
      return List.of();
    }

    return jdbcTemplate.query("""
      SELECT text, source, weight
      FROM (
        SELECT s.keyword AS text, 'curated' AS source, s.weight AS weight
        FROM search_suggestions s
        WHERE s.is_active = TRUE
          AND s.keyword_normalized LIKE :queryLike
        UNION ALL
        SELECT DISTINCT d.product_name AS text, 'product' AS source, 0 AS weight
        FROM product_search_documents d
        WHERE d.product_name_normalized LIKE :queryLike
        UNION ALL
        SELECT DISTINCT d.brand_name AS text, 'brand' AS source, 0 AS weight
        FROM product_search_documents d
        WHERE d.brand_name IS NOT NULL
          AND d.brand_name_normalized LIKE :queryLike
        UNION ALL
        SELECT DISTINCT d.type_name AS text, 'type' AS source, 0 AS weight
        FROM product_search_documents d
        WHERE d.type_name IS NOT NULL
          AND lower(d.type_name) LIKE :queryLike
      ) suggestions
      ORDER BY weight DESC, text ASC
      LIMIT 10
      """, new MapSqlParameterSource("queryLike", normalizedQuery + "%"), (rs, rowNum) -> new SearchSuggestionDTO(
      rs.getString("text"),
      rs.getString("source"),
      rs.getInt("weight")
    ));
  }

  @Override
  public void logQuery(String queryText, String normalizedQuery, int resultCount, long latencyMs) {
    jdbcTemplate.update("""
      INSERT INTO search_query_logs (id, query_text, query_normalized, locale, result_count, total_latency_ms)
      VALUES (:id, :queryText, :queryNormalized, 'vi', :resultCount, :latencyMs)
      """, new MapSqlParameterSource()
      .addValue("id", UUID.randomUUID().toString())
      .addValue("queryText", queryText)
      .addValue("queryNormalized", normalizedQuery)
      .addValue("resultCount", resultCount)
      .addValue("latencyMs", latencyMs));
  }

  @Override
  public void upsertZeroResultQuery(String queryText, String normalizedQuery) {
    if (normalizedQuery == null || normalizedQuery.isBlank()) {
      return;
    }

    jdbcTemplate.update("""
      INSERT INTO search_zero_result_queries (id, query_text, query_normalized, locale, occurrence_count, last_seen_at)
      VALUES (:id, :queryText, :normalizedQuery, 'vi', 1, CURRENT_TIMESTAMP)
      ON CONFLICT (query_normalized, locale)
      DO UPDATE SET occurrence_count = search_zero_result_queries.occurrence_count + 1,
                    last_seen_at = CURRENT_TIMESTAMP
      """, new MapSqlParameterSource()
      .addValue("id", UUID.randomUUID().toString())
      .addValue("queryText", queryText)
      .addValue("normalizedQuery", normalizedQuery));
  }

  private MapSqlParameterSource baseSearchParams(SearchProductsRequest request, SearchQueryContext queryContext) {
    String tsQuery = queryContext.expandedTerms().stream()
      .flatMap(term -> SearchNormalizer.tokenize(term).stream())
      .distinct()
      .reduce((left, right) -> left + " | " + right)
      .orElse("");

    List<String> effectiveExpandedTerms = queryContext.expandedTerms().isEmpty()
      && queryContext.normalizedQuery() != null
      && !queryContext.normalizedQuery().isBlank()
      ? List.of(queryContext.normalizedQuery())
      : queryContext.expandedTerms();

    return new MapSqlParameterSource()
      .addValue("locale", queryContext.locale())
      .addValue("typeIds", request.typeIds() == null || request.typeIds().isEmpty() ? List.of("__all__") : request.typeIds())
      .addValue("hasTypeIds", request.typeIds() != null && !request.typeIds().isEmpty())
      .addValue("minPrice", request.minPrice())
      .addValue("maxPrice", request.maxPrice())
      .addValue("inStock", request.inStock())
      .addValue("hasInStock", request.inStock() != null)
      .addValue("normalizedQuery", queryContext.normalizedQuery() == null ? "" : queryContext.normalizedQuery())
      .addValue("rawLowerQuery", queryContext.rawLowerQuery() == null ? "" : queryContext.rawLowerQuery())
      .addValue("normalizedSku", queryContext.normalizedSku() == null ? "" : queryContext.normalizedSku())
      .addValue("queryLike", "%" + (queryContext.normalizedQuery() == null ? "" : queryContext.normalizedQuery()) + "%")
      .addValue("skuPrefix", (queryContext.normalizedSku() == null ? "" : queryContext.normalizedSku()) + "%")
      .addValue("expandedTerms", effectiveExpandedTerms.isEmpty() ? List.of("__none__") : effectiveExpandedTerms)
      .addValue("tsQuery", tsQuery)
      .addValue("hasTsQuery", !tsQuery.isBlank());
  }

  private String buildLegacyFilters(String typeId, BigDecimal minPrice, BigDecimal maxPrice) {
    List<String> predicates = new ArrayList<>();
    predicates.add("1 = 1");

    if (typeId != null && !typeId.isBlank()) {
      predicates.add("d.type_id = :typeId");
    }
    if (minPrice != null) {
      predicates.add("d.min_price >= :minPrice");
    }
    if (maxPrice != null) {
      predicates.add("d.min_price <= :maxPrice");
    }

    return "WHERE " + String.join(" AND ", predicates);
  }

  private String buildSearchFilters(SearchProductsRequest request) {
    List<String> predicates = new ArrayList<>();
    predicates.add("1 = 1");

    if (request.typeIds() != null && !request.typeIds().isEmpty()) {
      predicates.add("d.type_id IN (:typeIds)");
    }
    if (request.minPrice() != null) {
      predicates.add("d.min_price >= :minPrice");
    }
    if (request.maxPrice() != null) {
      predicates.add("d.min_price <= :maxPrice");
    }
    if (request.inStock() != null) {
      predicates.add("d.in_stock = :inStock");
    }

    return String.join("\nAND ", predicates);
  }

  private String buildSearchCondition(String normalizedQuery) {
    if (normalizedQuery == null || normalizedQuery.isBlank()) {
      return "TRUE";
    }

    return """
      (
        lower(d.slug) = :rawLowerQuery
        OR d.product_name_normalized LIKE :queryLike
        OR COALESCE(d.brand_name_normalized, '') LIKE :queryLike
        OR COALESCE(lower(d.type_name), '') LIKE :queryLike
        OR EXISTS (
          SELECT 1
          FROM product_search_skus s
          WHERE s.product_id = d.product_id
            AND s.sku_normalized LIKE :skuPrefix
        )
        OR EXISTS (
          SELECT 1
          FROM product_search_facet_values f
          WHERE f.product_id = d.product_id
            AND f.facet_value IN (:expandedTerms)
        )
        OR (
          :hasTsQuery = TRUE
          AND (
            COALESCE(d.title_vector, ''::tsvector) @@ to_tsquery('simple', :tsQuery)
            OR COALESCE(d.keyword_vector, ''::tsvector) @@ to_tsquery('simple', :tsQuery)
            OR COALESCE(d.body_vector, ''::tsvector) @@ to_tsquery('simple', :tsQuery)
          )
        )
      )
      """;
  }

  private String resolveSearchOrder(SearchProductsRequest request) {
    return switch (request.sort() == null ? "relevance" : request.sort().toLowerCase()) {
      case "price_asc" -> "c.min_price ASC NULLS LAST, c.product_name ASC, c.product_id ASC";
      case "price_desc" -> "c.min_price DESC NULLS LAST, c.product_name ASC, c.product_id ASC";
      case "top_rated" -> "c.average_rating DESC NULLS LAST, c.review_count DESC, c.product_name ASC, c.product_id ASC";
      case "best_seller" -> "c.sold_count DESC, c.product_name ASC, c.product_id ASC";
      case "newest" -> "c.source_updated_at DESC NULLS LAST, c.product_name ASC, c.product_id ASC";
      default -> "total_score DESC, c.product_name ASC, c.product_id ASC";
    };
  }

  private static SearchProductItemDTO mapSearchItem(ResultSet rs, int rowNum) throws SQLException {
    return new SearchProductItemDTO(
      rs.getString("product_id"),
      rs.getString("slug"),
      rs.getString("product_name"),
      rs.getString("type_id"),
      rs.getString("type_name"),
      rs.getBigDecimal("min_price"),
      rs.getBigDecimal("max_price"),
      rs.getBoolean("in_stock"),
      rs.getInt("active_variant_count"),
      rs.getBigDecimal("average_rating"),
      rs.getInt("review_count"),
      rs.getInt("sold_count"),
      rs.getDouble("total_score")
    );
  }
}
