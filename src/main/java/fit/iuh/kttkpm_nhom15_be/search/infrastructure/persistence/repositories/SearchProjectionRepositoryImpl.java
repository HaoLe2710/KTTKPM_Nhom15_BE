package fit.iuh.kttkpm_nhom15_be.search.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.search.application.support.SearchNormalizer;
import fit.iuh.kttkpm_nhom15_be.search.domain.models.SearchProjectionTask;
import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchProjectionRepository;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class SearchProjectionRepositoryImpl implements SearchProjectionRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public void enqueueProjectionTask(String productId, String reason) {
    jdbcTemplate.update("""
      INSERT INTO search_projection_tasks (product_id, reason, status, attempt_count, next_attempt_at, created_at, updated_at)
      VALUES (:productId, :reason, 'PENDING', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
      ON CONFLICT (product_id)
      DO UPDATE SET reason = EXCLUDED.reason,
                    status = 'PENDING',
                    attempt_count = 0,
                    next_attempt_at = CURRENT_TIMESTAMP,
                    last_error = NULL,
                    updated_at = CURRENT_TIMESTAMP
      """, new MapSqlParameterSource()
      .addValue("productId", productId)
      .addValue("reason", reason));
  }

  @Override
  public List<SearchProjectionTask> findDueProjectionTasks(int limit) {
    return jdbcTemplate.query("""
      SELECT product_id, reason, attempt_count
      FROM search_projection_tasks
      WHERE status = 'PENDING'
        AND next_attempt_at <= CURRENT_TIMESTAMP
      ORDER BY next_attempt_at ASC, product_id ASC
      LIMIT :limit
      """, new MapSqlParameterSource("limit", limit), (rs, rowNum) -> new SearchProjectionTask(
      rs.getString("product_id"),
      rs.getString("reason"),
      rs.getInt("attempt_count")
    ));
  }

  @Override
  public void markTaskProcessing(String productId) {
    jdbcTemplate.update("""
      UPDATE search_projection_tasks
      SET status = 'PROCESSING',
          last_attempt_at = CURRENT_TIMESTAMP,
          updated_at = CURRENT_TIMESTAMP
      WHERE product_id = :productId
      """, new MapSqlParameterSource("productId", productId));
  }

  @Override
  public void markTaskSucceeded(String productId) {
    jdbcTemplate.update("DELETE FROM search_projection_tasks WHERE product_id = :productId",
      new MapSqlParameterSource("productId", productId));
    jdbcTemplate.update("""
      UPDATE search_projection_failures
      SET state = 'RESOLVED_AUTOMATIC',
          resolution_type = 'AUTOMATIC',
          resolved_at = CURRENT_TIMESTAMP,
          updated_at = CURRENT_TIMESTAMP
      WHERE product_id = :productId
        AND state IN ('OPEN', 'RETRY_QUEUED')
      """, new MapSqlParameterSource("productId", productId));
  }

  @Override
  public void markTaskRetry(String productId, int attemptCount, Duration delay, String errorMessage) {
    jdbcTemplate.update("""
      UPDATE search_projection_tasks
      SET status = 'PENDING',
          attempt_count = :attemptCount,
          next_attempt_at = :nextAttemptAt,
          last_error = :errorMessage,
          updated_at = CURRENT_TIMESTAMP
      WHERE product_id = :productId
      """, new MapSqlParameterSource()
      .addValue("productId", productId)
      .addValue("attemptCount", attemptCount)
      .addValue("nextAttemptAt", Timestamp.valueOf(LocalDateTime.now().plus(delay)))
      .addValue("errorMessage", errorMessage));
  }

  @Override
  public void markTaskPermanentFailure(String productId, int attemptCount, String eventType, String errorMessage) {
    jdbcTemplate.update("""
      UPDATE search_projection_tasks
      SET status = 'PERMANENT_FAILURE',
          attempt_count = :attemptCount,
          last_error = :errorMessage,
          updated_at = CURRENT_TIMESTAMP
      WHERE product_id = :productId
      """, new MapSqlParameterSource()
      .addValue("productId", productId)
      .addValue("attemptCount", attemptCount)
      .addValue("errorMessage", errorMessage));

    jdbcTemplate.update("""
      INSERT INTO search_projection_failures (
        id, product_id, event_type, error_message, retry_count, failed_at, next_reconciliation_at, state, updated_at
      ) VALUES (
        :id, :productId, :eventType, :errorMessage, :retryCount, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '24 hours', 'OPEN', CURRENT_TIMESTAMP
      )
      """, new MapSqlParameterSource()
      .addValue("id", UUID.randomUUID().toString())
      .addValue("productId", productId)
      .addValue("eventType", eventType)
      .addValue("errorMessage", errorMessage)
      .addValue("retryCount", attemptCount));
  }

  @Override
  public String startProjectionRun(String runType) {
    String runId = UUID.randomUUID().toString();
    jdbcTemplate.update("""
      INSERT INTO search_projection_runs (id, run_type, status, started_at)
      VALUES (:id, :runType, 'RUNNING', CURRENT_TIMESTAMP)
      """, new MapSqlParameterSource()
      .addValue("id", runId)
      .addValue("runType", runType));
    return runId;
  }

  @Override
  public void finishProjectionRun(String runId, String status, String cursorProductId, int processedCount, int failedCount) {
    jdbcTemplate.update("""
      UPDATE search_projection_runs
      SET status = :status,
          cursor_product_id = :cursorProductId,
          processed_count = :processedCount,
          failed_count = :failedCount,
          finished_at = CURRENT_TIMESTAMP
      WHERE id = :id
      """, new MapSqlParameterSource()
      .addValue("id", runId)
      .addValue("status", status)
      .addValue("cursorProductId", cursorProductId)
      .addValue("processedCount", processedCount)
      .addValue("failedCount", failedCount));
  }

  @Override
  public List<String> findActiveProductIdsAfter(String cursorProductId, int limit) {
    if (cursorProductId == null || cursorProductId.isBlank()) {
      return jdbcTemplate.query("""
        SELECT p.id
        FROM products p
        WHERE p.is_active = TRUE
        ORDER BY p.id ASC
        LIMIT :limit
        """, new MapSqlParameterSource()
        .addValue("limit", limit), (rs, rowNum) -> rs.getString("id"));
    }

    return jdbcTemplate.query("""
      SELECT p.id
      FROM products p
      WHERE p.is_active = TRUE
        AND p.id > :cursorProductId
      ORDER BY p.id ASC
      LIMIT :limit
      """, new MapSqlParameterSource()
      .addValue("cursorProductId", cursorProductId)
      .addValue("limit", limit), (rs, rowNum) -> rs.getString("id"));
  }

  @Override
  public List<String> findStaleOrMissingProductIds(int limit) {
    return jdbcTemplate.query("""
      SELECT p.id
      FROM products p
      LEFT JOIN product_search_documents d ON d.product_id = p.id
      WHERE p.is_active = TRUE
        AND (
          d.product_id IS NULL
          OR d.source_updated_at < 
      """ + sourceUpdatedAtSql("p") + """
        )
      ORDER BY p.id ASC
      LIMIT :limit
      """, new MapSqlParameterSource("limit", limit), (rs, rowNum) -> rs.getString("id"));
  }

  @Override
  @Transactional
  public void rebuildProductProjection(String productId) {
    ProjectionSourceRow source = fetchProjectionSource(productId);
    if (source == null) {
      deleteProjection(productId);
      return;
    }

    List<String> ingredientNames = findSemanticNames(productId, "product_ingredients", "ingredient_id", "ingredients");
    List<String> skinTypeNames = findSemanticNames(productId, "product_skin_types", "skin_type_id", "skin_types");
    List<String> concernNames = findSemanticNames(productId, "product_concerns", "concern_id", "concerns");
    List<String> tagNames = findSemanticNames(productId, "product_tags", "tag_id", "tags");
    String productNameNormalized = SearchNormalizer.normalizeText(source.productName());
    String brandNameNormalized = SearchNormalizer.normalizeText(source.brandName());
    String documentText = buildDocumentText(source, ingredientNames, skinTypeNames, concernNames, tagNames);
    String keywordText = buildKeywordText(source, ingredientNames, skinTypeNames, concernNames, tagNames);
    String bodyText = SearchNormalizer.normalizeText(Objects.toString(source.descriptionMd(), ""));

    jdbcTemplate.update("""
      INSERT INTO product_search_documents (
        product_id, locale, slug, product_name, product_name_normalized, brand_id, brand_name, brand_name_normalized,
        type_id, type_name, short_description, min_price, max_price, in_stock, active_variant_count, average_rating,
        review_count, sold_count, is_featured, is_new, is_best_seller, manual_boost, source_updated_at, projection_updated_at,
        projection_version, title_vector, keyword_vector, body_vector, document_text
      ) VALUES (
        :productId, 'vi', :slug, :productName, :productNameNormalized, :brandId, :brandName, :brandNameNormalized,
        :typeId, :typeName, :shortDescription, :minPrice, :maxPrice, :inStock, :activeVariantCount, :averageRating,
        :reviewCount, :soldCount, FALSE, FALSE, FALSE, :manualBoost, :sourceUpdatedAt, CURRENT_TIMESTAMP,
        COALESCE((SELECT projection_version + 1 FROM product_search_documents WHERE product_id = :productId), 1),
        to_tsvector('simple', :titleText), to_tsvector('simple', :keywordText), to_tsvector('simple', :bodyText), :documentText
      )
      ON CONFLICT (product_id)
      DO UPDATE SET slug = EXCLUDED.slug,
                    product_name = EXCLUDED.product_name,
                    product_name_normalized = EXCLUDED.product_name_normalized,
                    brand_id = EXCLUDED.brand_id,
                    brand_name = EXCLUDED.brand_name,
                    brand_name_normalized = EXCLUDED.brand_name_normalized,
                    type_id = EXCLUDED.type_id,
                    type_name = EXCLUDED.type_name,
                    short_description = EXCLUDED.short_description,
                    min_price = EXCLUDED.min_price,
                    max_price = EXCLUDED.max_price,
                    in_stock = EXCLUDED.in_stock,
                    active_variant_count = EXCLUDED.active_variant_count,
                    average_rating = EXCLUDED.average_rating,
                    review_count = EXCLUDED.review_count,
                    sold_count = EXCLUDED.sold_count,
                    manual_boost = EXCLUDED.manual_boost,
                    source_updated_at = EXCLUDED.source_updated_at,
                    projection_updated_at = CURRENT_TIMESTAMP,
                    projection_version = product_search_documents.projection_version + 1,
                    title_vector = EXCLUDED.title_vector,
                    keyword_vector = EXCLUDED.keyword_vector,
                    body_vector = EXCLUDED.body_vector,
                    document_text = EXCLUDED.document_text
      """, new MapSqlParameterSource()
      .addValue("productId", source.productId())
      .addValue("slug", source.slug())
      .addValue("productName", source.productName())
      .addValue("productNameNormalized", productNameNormalized)
      .addValue("brandId", source.brandId())
      .addValue("brandName", source.brandName())
      .addValue("brandNameNormalized", brandNameNormalized.isBlank() ? null : brandNameNormalized)
      .addValue("typeId", source.typeId())
      .addValue("typeName", source.typeName())
      .addValue("shortDescription", source.shortDescription())
      .addValue("minPrice", source.minPrice())
      .addValue("maxPrice", source.maxPrice())
      .addValue("inStock", source.inStock())
      .addValue("activeVariantCount", source.activeVariantCount())
      .addValue("averageRating", source.averageRating())
      .addValue("reviewCount", source.reviewCount())
      .addValue("soldCount", source.soldCount())
      .addValue("manualBoost", source.manualBoost())
      .addValue("sourceUpdatedAt", Timestamp.valueOf(source.sourceUpdatedAt()))
      .addValue("titleText", productNameNormalized)
      .addValue("keywordText", keywordText)
      .addValue("bodyText", bodyText)
      .addValue("documentText", documentText));

    replaceSkus(productId);
    replaceFacetValues(productId, ingredientNames, skinTypeNames, concernNames, tagNames);
  }

  private ProjectionSourceRow fetchProjectionSource(String productId) {
    try {
      return jdbcTemplate.queryForObject("""
        SELECT p.id AS product_id,
               p.slug,
               p.name AS product_name,
               p.description_md,
               p.short_description,
               pt.id AS type_id,
               pt.name AS type_name,
               p.brand_id,
               b.name AS brand_name,
               (SELECT MIN(v.price) FROM variants v WHERE v.product_id = p.id AND v.is_active = TRUE) AS min_price,
               (SELECT MAX(v.price) FROM variants v WHERE v.product_id = p.id AND v.is_active = TRUE) AS max_price,
               COALESCE((SELECT COUNT(*) FROM variants v WHERE v.product_id = p.id AND v.is_active = TRUE), 0) AS active_variant_count,
               COALESCE((SELECT SUM(v.stock_quantity) FROM variants v WHERE v.product_id = p.id AND v.is_active = TRUE), 0) > 0 AS in_stock,
               COALESCE((SELECT AVG(r.rating) FROM reviews r WHERE r.product_id = p.id), 0) AS average_rating,
               COALESCE((SELECT COUNT(*) FROM reviews r WHERE r.product_id = p.id), 0) AS review_count,
               COALESCE((
                   SELECT SUM(oi.quantity)
                   FROM order_items oi
                   JOIN variants v ON v.id = oi.variant_id
                   JOIN orders o ON o.id = oi.order_id
                   WHERE v.product_id = p.id
                     AND o.status <> 'CANCELLED'
               ), 0) AS sold_count,
               COALESCE((
                   SELECT boost_value
                   FROM search_product_boost_overrides spbo
                WHERE spbo.product_id = p.id
                  AND (spbo.starts_at IS NULL OR spbo.starts_at <= CURRENT_TIMESTAMP)
                  AND (spbo.ends_at IS NULL OR spbo.ends_at >= CURRENT_TIMESTAMP)
                ORDER BY spbo.updated_at DESC
                LIMIT 1
               ), 0) AS manual_boost,
               %s AS source_updated_at
         FROM products p
         LEFT JOIN brands b ON b.id = p.brand_id
         LEFT JOIN product_types pt ON pt.id = p.type_id
         WHERE p.id = :productId
           AND p.is_active = TRUE
         """.formatted(sourceUpdatedAtSql("p")),
         new MapSqlParameterSource("productId", productId),
         (rs, rowNum) -> new ProjectionSourceRow(
          rs.getString("product_id"),
          rs.getString("slug"),
          rs.getString("product_name"),
          rs.getString("description_md"),
          rs.getString("short_description"),
          rs.getString("type_id"),
          rs.getString("type_name"),
          rs.getString("brand_id"),
          rs.getString("brand_name"),
          rs.getBigDecimal("min_price"),
          rs.getBigDecimal("max_price"),
          rs.getBoolean("in_stock"),
          rs.getInt("active_variant_count"),
          rs.getBigDecimal("average_rating"),
          rs.getInt("review_count"),
          rs.getInt("sold_count"),
          rs.getInt("manual_boost"),
          rs.getTimestamp("source_updated_at").toLocalDateTime()
        ));
    } catch (EmptyResultDataAccessException ignored) {
      return null;
    }
  }

  private void replaceSkus(String productId) {
    jdbcTemplate.update("DELETE FROM product_search_skus WHERE product_id = :productId",
      new MapSqlParameterSource("productId", productId));

    List<Map<String, Object>> skuRows = jdbcTemplate.queryForList("""
      SELECT id, sku
      FROM variants
      WHERE product_id = :productId
        AND is_active = TRUE
      """, new MapSqlParameterSource("productId", productId));

    for (Map<String, Object> skuRow : skuRows) {
      String skuRaw = Objects.toString(skuRow.get("sku"), "");
      jdbcTemplate.update("""
        INSERT INTO product_search_skus (variant_id, product_id, sku_raw, sku_normalized)
        VALUES (:variantId, :productId, :skuRaw, :skuNormalized)
        """, new MapSqlParameterSource()
        .addValue("variantId", skuRow.get("id"))
        .addValue("productId", productId)
        .addValue("skuRaw", skuRaw)
        .addValue("skuNormalized", SearchNormalizer.normalizeSku(skuRaw)));
    }
  }

  private void replaceFacetValues(String productId,
                                  List<String> ingredientNames,
                                  List<String> skinTypeNames,
                                  List<String> concernNames,
                                  List<String> tagNames) {
    jdbcTemplate.update("DELETE FROM product_search_facet_values WHERE product_id = :productId",
      new MapSqlParameterSource("productId", productId));

    List<Map<String, Object>> optionRows = jdbcTemplate.queryForList("""
      SELECT o.name AS option_name, ov.value AS option_value
      FROM variants v
      JOIN variant_options vo ON vo.variant_id = v.id
      JOIN option_values ov ON ov.id = vo.option_value_id
      JOIN options o ON o.id = ov.option_id
      WHERE v.product_id = :productId
        AND v.is_active = TRUE
        AND ov.is_active = TRUE
      ORDER BY o.name ASC, ov.value ASC
      """, new MapSqlParameterSource("productId", productId));

    for (Map<String, Object> optionRow : optionRows) {
      String optionName = Objects.toString(optionRow.get("option_name"), "");
      String optionValue = Objects.toString(optionRow.get("option_value"), "");
      jdbcTemplate.update("""
        INSERT INTO product_search_facet_values (product_id, facet_key, facet_value, facet_label, sort_order)
        VALUES (:productId, 'attribute', :facetValue, :facetLabel, 0)
        ON CONFLICT (product_id, facet_key, facet_value) DO NOTHING
        """, new MapSqlParameterSource()
        .addValue("productId", productId)
        .addValue("facetValue", SearchNormalizer.normalizeText(optionValue))
        .addValue("facetLabel", optionName + ": " + optionValue));
    }

    insertFacetValues(productId, "ingredient", ingredientNames, 10);
    insertFacetValues(productId, "skin_type", skinTypeNames, 20);
    insertFacetValues(productId, "concern", concernNames, 30);
    insertFacetValues(productId, "tag", tagNames, 40);
  }

  private void deleteProjection(String productId) {
    jdbcTemplate.update("DELETE FROM product_search_facet_values WHERE product_id = :productId",
      new MapSqlParameterSource("productId", productId));
    jdbcTemplate.update("DELETE FROM product_search_skus WHERE product_id = :productId",
      new MapSqlParameterSource("productId", productId));
    jdbcTemplate.update("DELETE FROM product_search_documents WHERE product_id = :productId",
      new MapSqlParameterSource("productId", productId));
  }

  private String buildDocumentText(ProjectionSourceRow source,
                                   List<String> ingredientNames,
                                   List<String> skinTypeNames,
                                   List<String> concernNames,
                                   List<String> tagNames) {
    StringBuilder builder = new StringBuilder();
    append(builder, SearchNormalizer.normalizeText(source.productName()));
    append(builder, SearchNormalizer.normalizeText(source.brandName()));
    append(builder, SearchNormalizer.normalizeText(source.typeName()));
    append(builder, SearchNormalizer.normalizeText(source.shortDescription()));
    append(builder, SearchNormalizer.normalizeText(source.descriptionMd()));
    ingredientNames.forEach(name -> append(builder, SearchNormalizer.normalizeText(name)));
    skinTypeNames.forEach(name -> append(builder, SearchNormalizer.normalizeText(name)));
    concernNames.forEach(name -> append(builder, SearchNormalizer.normalizeText(name)));
    tagNames.forEach(name -> append(builder, SearchNormalizer.normalizeText(name)));

    List<String> skus = jdbcTemplate.query("""
      SELECT sku
      FROM variants
      WHERE product_id = :productId
        AND is_active = TRUE
      ORDER BY sku ASC
      """, new MapSqlParameterSource("productId", source.productId()), (rs, rowNum) -> rs.getString("sku"));
    skus.forEach(sku -> append(builder, SearchNormalizer.normalizeSku(sku)));
    return builder.toString().trim();
  }

  private String buildKeywordText(ProjectionSourceRow source,
                                  List<String> ingredientNames,
                                  List<String> skinTypeNames,
                                  List<String> concernNames,
                                  List<String> tagNames) {
    StringBuilder builder = new StringBuilder();
    append(builder, SearchNormalizer.normalizeText(source.productName()));
    append(builder, SearchNormalizer.normalizeText(source.brandName()));
    append(builder, SearchNormalizer.normalizeText(source.typeName()));
    ingredientNames.forEach(name -> append(builder, SearchNormalizer.normalizeText(name)));
    skinTypeNames.forEach(name -> append(builder, SearchNormalizer.normalizeText(name)));
    concernNames.forEach(name -> append(builder, SearchNormalizer.normalizeText(name)));
    tagNames.forEach(name -> append(builder, SearchNormalizer.normalizeText(name)));
    return builder.toString().trim();
  }

  private List<String> findSemanticNames(String productId,
                                         String relationTable,
                                         String relationIdColumn,
                                         String masterTable) {
    return jdbcTemplate.query(
      "SELECT m.name FROM " + relationTable + " r "
        + "JOIN " + masterTable + " m ON m.id = r." + relationIdColumn + " "
        + "WHERE r.product_id = :productId ORDER BY m.name ASC, m.id ASC",
      new MapSqlParameterSource("productId", productId),
      (rs, rowNum) -> rs.getString("name")
    );
  }

  private void insertFacetValues(String productId, String facetKey, List<String> labels, int sortOrder) {
    for (String label : labels) {
      String normalized = SearchNormalizer.normalizeText(label);
      if (normalized.isBlank()) {
        continue;
      }
      jdbcTemplate.update("""
        INSERT INTO product_search_facet_values (product_id, facet_key, facet_value, facet_label, sort_order)
        VALUES (:productId, :facetKey, :facetValue, :facetLabel, :sortOrder)
        ON CONFLICT (product_id, facet_key, facet_value) DO NOTHING
        """, new MapSqlParameterSource()
        .addValue("productId", productId)
        .addValue("facetKey", facetKey)
        .addValue("facetValue", normalized)
        .addValue("facetLabel", label)
        .addValue("sortOrder", sortOrder));
    }
  }

  private void append(StringBuilder builder, String value) {
    if (value == null || value.isBlank()) {
      return;
    }
    if (!builder.isEmpty()) {
      builder.append(' ');
    }
    builder.append(value);
  }

  private String sourceUpdatedAtSql(String productAlias) {
    return "GREATEST("
      + "COALESCE(" + productAlias + ".updated_at, " + productAlias + ".created_at), "
      + "COALESCE((SELECT MAX(v.updated_at) FROM variants v WHERE v.product_id = " + productAlias + ".id), " + productAlias + ".created_at), "
      + "COALESCE((SELECT MAX(r.created_at) FROM reviews r WHERE r.product_id = " + productAlias + ".id), " + productAlias + ".created_at), "
      + "COALESCE((SELECT MAX(o.updated_at) FROM orders o "
      + "JOIN order_items oi ON oi.order_id = o.id "
      + "JOIN variants v2 ON v2.id = oi.variant_id "
      + "WHERE v2.product_id = " + productAlias + ".id), " + productAlias + ".created_at), "
      + "COALESCE((SELECT MAX(spbo.updated_at) FROM search_product_boost_overrides spbo WHERE spbo.product_id = " + productAlias + ".id), "
      + productAlias + ".created_at), "
      + "COALESCE((SELECT MAX(COALESCE(b.updated_at, b.created_at)) FROM brands b WHERE b.id = " + productAlias + ".brand_id), " + productAlias + ".created_at), "
      + relationSourceUpdatedAtSql(productAlias, "product_ingredients", "pi", "ingredient_id", "ingredients", "i") + ", "
      + relationSourceUpdatedAtSql(productAlias, "product_skin_types", "pst", "skin_type_id", "skin_types", "st") + ", "
      + relationSourceUpdatedAtSql(productAlias, "product_concerns", "pc", "concern_id", "concerns", "c") + ", "
      + relationSourceUpdatedAtSql(productAlias, "product_tags", "ptg", "tag_id", "tags", "t")
      + ")";
  }

  private String relationSourceUpdatedAtSql(String productAlias,
                                            String relationTable,
                                            String relationAlias,
                                            String relationMasterColumn,
                                            String masterTable,
                                            String masterAlias) {
    return "COALESCE((SELECT MAX(GREATEST(COALESCE(" + relationAlias + ".updated_at, " + relationAlias + ".created_at), "
      + "COALESCE(" + masterAlias + ".updated_at, " + masterAlias + ".created_at))) "
      + "FROM " + relationTable + " " + relationAlias + " "
      + "JOIN " + masterTable + " " + masterAlias + " ON " + masterAlias + ".id = " + relationAlias + "." + relationMasterColumn + " "
      + "WHERE " + relationAlias + ".product_id = " + productAlias + ".id), " + productAlias + ".created_at)";
  }

  private record ProjectionSourceRow(
    String productId,
    String slug,
    String productName,
    String descriptionMd,
    String shortDescription,
    String typeId,
    String typeName,
    String brandId,
    String brandName,
    BigDecimal minPrice,
    BigDecimal maxPrice,
    boolean inStock,
    int activeVariantCount,
    BigDecimal averageRating,
    int reviewCount,
    int soldCount,
    int manualBoost,
    LocalDateTime sourceUpdatedAt
  ) {}
}
