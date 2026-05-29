package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.CatalogHealthIssueCountResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.CatalogHealthResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionAssignmentResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionValueListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductDetailResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.SearchStatusResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.SemanticMasterListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.SemanticReferenceResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.VariantSummaryResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.SemanticMasterKind;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.CatalogAdminRepository;
import fit.iuh.kttkpm_nhom15_be.search.application.support.SearchNormalizer;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CatalogAdminRepositoryImpl implements CatalogAdminRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public Page<ProductListItemResponse> findProducts(String query, String typeId, Boolean active, AdminPageRequest pageRequest) {
    String where = buildProductWhereClause(query, typeId, active);
    MapSqlParameterSource params = new MapSqlParameterSource()
      .addValue("queryLike", query == null || query.isBlank() ? null : "%" + SearchNormalizer.normalizeText(query) + "%")
      .addValue("typeId", typeId)
      .addValue("active", active)
      .addValue("limit", pageRequest.size())
      .addValue("offset", pageRequest.page() * pageRequest.size());

    String listSql = """
      SELECT p.id,
             p.type_id,
             p.name,
             p.slug,
             p.is_active,
             stats.min_price,
             stats.max_price,
             stats.total_stock,
             stats.active_variant_count,
             d.projection_updated_at,
             CASE
               WHEN d.product_id IS NULL THEN 'MISSING'
               WHEN d.source_updated_at < 
      """ + sourceUpdatedAtSql("p") + """
               THEN 'STALE'
               ELSE 'FRESH'
             END AS projection_staleness
      FROM products p
      LEFT JOIN product_search_documents d ON d.product_id = p.id
      LEFT JOIN LATERAL (
        SELECT MIN(v.price) AS min_price,
               MAX(v.price) AS max_price,
               COALESCE(SUM(CASE WHEN v.is_active THEN v.stock_quantity ELSE 0 END), 0) AS total_stock,
               COUNT(*) FILTER (WHERE v.is_active) AS active_variant_count
        FROM variants v
        WHERE v.product_id = p.id
      ) stats ON TRUE
      """
      + "\n" + where + "\n"
      + "ORDER BY " + productOrderBy(pageRequest) + "\n"
      + "LIMIT :limit OFFSET :offset";

    List<ProductListItemResponse> content = jdbcTemplate.query(listSql, params, (rs, rowNum) -> new ProductListItemResponse(
      rs.getString("id"),
      rs.getString("type_id"),
      rs.getString("name"),
      rs.getString("slug"),
      rs.getBoolean("is_active"),
      rs.getBigDecimal("min_price"),
      rs.getBigDecimal("max_price"),
      rs.getLong("total_stock"),
      rs.getLong("active_variant_count"),
      rs.getString("projection_staleness"),
      timestampToLocalDateTime(rs.getTimestamp("projection_updated_at"))
    ));

    Long total = jdbcTemplate.queryForObject("""
      SELECT COUNT(*)
      FROM products p
      """
      + "\n" + where, params, Long.class);

    return new PageImpl<>(content, PageRequest.of(pageRequest.page(), pageRequest.size()), total == null ? 0 : total);
  }

  @Override
  public Optional<ProductDetailResponse> findProductDetail(String productId) {
    String detailSql = """
      SELECT p.id,
             p.type_id,
             p.name,
             p.slug,
             p.description_md,
             p.short_description,
             p.brand_id,
             b.name AS brand_name,
             p.is_customizable,
             p.is_active,
             stats.min_price,
             stats.max_price,
             stats.total_stock,
             stats.active_variant_count,
             COALESCE(d.review_count, 0) AS review_count,
             COALESCE(d.average_rating, 0) AS average_rating,
             COALESCE(d.sold_count, 0) AS sold_count,
             d.product_id AS projection_product_id,
             d.projection_updated_at,
      """ + sourceUpdatedAtSql("p") + """
             AS source_updated_at,
             COALESCE(d.projection_version, 0) AS projection_version,
             CASE
               WHEN d.product_id IS NULL THEN 'MISSING'
               WHEN d.source_updated_at < 
      """ + sourceUpdatedAtSql("p") + """
               THEN 'STALE'
               ELSE 'FRESH'
             END AS projection_staleness
      FROM products p
      LEFT JOIN brands b ON b.id = p.brand_id
      LEFT JOIN product_search_documents d ON d.product_id = p.id
      LEFT JOIN LATERAL (
        SELECT MIN(v.price) AS min_price,
               MAX(v.price) AS max_price,
               COALESCE(SUM(CASE WHEN v.is_active THEN v.stock_quantity ELSE 0 END), 0) AS total_stock,
               COUNT(*) FILTER (WHERE v.is_active) AS active_variant_count
        FROM variants v
        WHERE v.product_id = p.id
      ) stats ON TRUE
      WHERE p.id = :productId
      """;

    List<ProductDetailResponse> details = jdbcTemplate.query(detailSql, new MapSqlParameterSource("productId", productId), (rs, rowNum) -> new ProductDetailResponse(
      rs.getString("id"),
      rs.getString("type_id"),
      rs.getString("name"),
      rs.getString("slug"),
      rs.getString("description_md"),
      rs.getString("short_description"),
      rs.getString("brand_id"),
      rs.getString("brand_name"),
      rs.getBoolean("is_customizable"),
      rs.getBoolean("is_active"),
      rs.getBigDecimal("min_price"),
      rs.getBigDecimal("max_price"),
      rs.getLong("total_stock"),
      rs.getLong("active_variant_count"),
      rs.getLong("review_count"),
      rs.getBigDecimal("average_rating"),
      rs.getLong("sold_count"),
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      new SearchStatusResponse(
        rs.getString("projection_product_id") != null,
        timestampToLocalDateTime(rs.getTimestamp("projection_updated_at")),
        timestampToLocalDateTime(rs.getTimestamp("source_updated_at")),
        rs.getLong("projection_version"),
        rs.getString("projection_staleness")
      ),
      List.of()
    ));

    if (details.isEmpty()) {
      return Optional.empty();
    }

    ProductDetailResponse detail = details.getFirst();
    return Optional.of(new ProductDetailResponse(
      detail.id(),
      detail.typeId(),
      detail.name(),
      detail.slug(),
      detail.descriptionMd(),
      detail.shortDescription(),
      detail.brandId(),
      detail.brandName(),
      detail.customizable(),
      detail.active(),
      detail.minPrice(),
      detail.maxPrice(),
      detail.totalStock(),
      detail.activeVariantCount(),
      detail.reviewCount(),
      detail.averageRating(),
      detail.soldCount(),
      findProductSemanticReferences(productId, SemanticMasterKind.INGREDIENT),
      findProductSemanticReferences(productId, SemanticMasterKind.SKIN_TYPE),
      findProductSemanticReferences(productId, SemanticMasterKind.CONCERN),
      findProductSemanticReferences(productId, SemanticMasterKind.TAG),
      detail.searchStatus(),
      findVariantSummaries(productId)
    ));
  }

  @Override
  public Optional<SearchStatusResponse> findSearchStatus(String productId) {
    return findProductDetail(productId).map(ProductDetailResponse::searchStatus);
  }

  @Override
  public boolean existsProduct(String productId) {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
      SELECT EXISTS(SELECT 1 FROM products WHERE id = :productId)
      """, new MapSqlParameterSource("productId", productId), Boolean.class));
  }

  @Override
  public boolean existsProductType(String typeId) {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
      SELECT EXISTS(SELECT 1 FROM product_types WHERE id = :typeId)
      """, new MapSqlParameterSource("typeId", typeId), Boolean.class));
  }

  @Override
  public boolean existsProductSlugIgnoreCase(String slug, String excludeProductId) {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
      SELECT EXISTS(
        SELECT 1
        FROM products
        WHERE LOWER(slug) = LOWER(:slug)
          AND (CAST(:excludeProductId AS VARCHAR) IS NULL OR id <> CAST(:excludeProductId AS VARCHAR))
      )
      """, new MapSqlParameterSource()
      .addValue("slug", slug)
      .addValue("excludeProductId", excludeProductId), Boolean.class));
  }

  @Override
  public String insertProduct(String typeId,
                              String name,
                              String slug,
                              String descriptionMd,
                              String shortDescription,
                              String brandId,
                              boolean customizable,
                              boolean active) {
    String productId = UUID.randomUUID().toString();
    jdbcTemplate.update("""
      INSERT INTO products (
        id, created_at, updated_at, type_id, name, slug, description_md, short_description, brand_id, is_customizable, is_active
      )
      VALUES (
        :id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :typeId, :name, :slug, :descriptionMd, :shortDescription, :brandId, :customizable, :active
      )
      """, new MapSqlParameterSource()
      .addValue("id", productId)
      .addValue("typeId", typeId)
      .addValue("name", name)
      .addValue("slug", slug)
      .addValue("descriptionMd", descriptionMd)
      .addValue("shortDescription", shortDescription)
      .addValue("brandId", brandId)
      .addValue("customizable", customizable)
      .addValue("active", active));
    return productId;
  }

  @Override
  public void updateProduct(String productId,
                            String typeId,
                            String name,
                            String slug,
                            String descriptionMd,
                            String shortDescription,
                            String brandId,
                            boolean customizable,
                            boolean active) {
    jdbcTemplate.update("""
      UPDATE products
      SET updated_at = CURRENT_TIMESTAMP,
          type_id = :typeId,
          name = :name,
          slug = :slug,
          description_md = :descriptionMd,
          short_description = :shortDescription,
          brand_id = :brandId,
          is_customizable = :customizable,
          is_active = :active
      WHERE id = :productId
      """, new MapSqlParameterSource()
      .addValue("productId", productId)
      .addValue("typeId", typeId)
      .addValue("name", name)
      .addValue("slug", slug)
      .addValue("descriptionMd", descriptionMd)
      .addValue("shortDescription", shortDescription)
      .addValue("brandId", brandId)
      .addValue("customizable", customizable)
      .addValue("active", active));
  }

  @Override
  public void updateProductActive(String productId, boolean active) {
    jdbcTemplate.update("""
      UPDATE products
      SET updated_at = CURRENT_TIMESTAMP,
          is_active = :active
      WHERE id = :productId
      """, new MapSqlParameterSource()
      .addValue("productId", productId)
      .addValue("active", active));
  }

  @Override
  public long countActiveVariants(String productId) {
    Long result = jdbcTemplate.queryForObject("""
      SELECT COUNT(*)
      FROM variants
      WHERE product_id = :productId
        AND is_active = TRUE
      """, new MapSqlParameterSource("productId", productId), Long.class);
    return result == null ? 0 : result;
  }

  @Override
  public boolean existsVariant(String variantId) {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
      SELECT EXISTS(SELECT 1 FROM variants WHERE id = :variantId)
      """, new MapSqlParameterSource("variantId", variantId), Boolean.class));
  }

  @Override
  public String findProductIdByVariantId(String variantId) {
    return jdbcTemplate.queryForObject("""
      SELECT product_id
      FROM variants
      WHERE id = :variantId
      """, new MapSqlParameterSource("variantId", variantId), String.class);
  }

  @Override
  public boolean existsNormalizedSku(String normalizedSku, String excludeVariantId) {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
      SELECT EXISTS(
        SELECT 1
        FROM variants
        WHERE REGEXP_REPLACE(UPPER(sku), '[\\s\\-_.\\/]', '', 'g') = :normalizedSku
          AND (CAST(:excludeVariantId AS VARCHAR) IS NULL OR id <> CAST(:excludeVariantId AS VARCHAR))
      )
      """, new MapSqlParameterSource()
      .addValue("normalizedSku", normalizedSku)
      .addValue("excludeVariantId", excludeVariantId), Boolean.class));
  }

  @Override
  public String insertVariant(String productId, String sku, BigDecimal price, int stockQuantity, boolean active) {
    String variantId = UUID.randomUUID().toString();
    jdbcTemplate.update("""
      INSERT INTO variants (id, created_at, updated_at, product_id, sku, price, stock_quantity, is_active)
      VALUES (:id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :productId, :sku, :price, :stockQuantity, :active)
      """, new MapSqlParameterSource()
      .addValue("id", variantId)
      .addValue("productId", productId)
      .addValue("sku", sku)
      .addValue("price", price)
      .addValue("stockQuantity", stockQuantity)
      .addValue("active", active));
    return variantId;
  }

  @Override
  public void updateVariant(String variantId, String sku, BigDecimal price, int stockQuantity, boolean active) {
    jdbcTemplate.update("""
      UPDATE variants
      SET updated_at = CURRENT_TIMESTAMP,
          sku = :sku,
          price = :price,
          stock_quantity = :stockQuantity,
          is_active = :active
      WHERE id = :variantId
      """, new MapSqlParameterSource()
      .addValue("variantId", variantId)
      .addValue("sku", sku)
      .addValue("price", price)
      .addValue("stockQuantity", stockQuantity)
      .addValue("active", active));
  }

  @Override
  public void updateVariantStock(String variantId, int stockQuantity) {
    jdbcTemplate.update("""
      UPDATE variants
      SET updated_at = CURRENT_TIMESTAMP,
          stock_quantity = :stockQuantity
      WHERE id = :variantId
      """, new MapSqlParameterSource()
      .addValue("variantId", variantId)
      .addValue("stockQuantity", stockQuantity));
  }

  @Override
  public void updateVariantActive(String variantId, boolean active) {
    jdbcTemplate.update("""
      UPDATE variants
      SET updated_at = CURRENT_TIMESTAMP,
          is_active = :active
      WHERE id = :variantId
      """, new MapSqlParameterSource()
      .addValue("variantId", variantId)
      .addValue("active", active));
  }

  @Override
  public void replaceVariantOptions(String variantId, List<String> optionValueIds) {
    jdbcTemplate.update("DELETE FROM variant_options WHERE variant_id = :variantId", new MapSqlParameterSource("variantId", variantId));
    for (String optionValueId : optionValueIds) {
      jdbcTemplate.update("""
        INSERT INTO variant_options (id, variant_id, option_value_id)
        VALUES (:id, :variantId, :optionValueId)
        """, new MapSqlParameterSource()
        .addValue("id", UUID.randomUUID().toString())
        .addValue("variantId", variantId)
        .addValue("optionValueId", optionValueId));
    }
  }

  @Override
  public long countActiveAssignableOptionValues(List<String> optionValueIds) {
    if (optionValueIds == null || optionValueIds.isEmpty()) {
      return 0;
    }
    Long result = jdbcTemplate.queryForObject("""
      SELECT COUNT(DISTINCT ov.id)
      FROM option_values ov
      JOIN options o ON o.id = ov.option_id
      WHERE ov.id IN (:ids)
        AND ov.is_active = TRUE
        AND COALESCE(o.is_active, TRUE) = TRUE
      """, new MapSqlParameterSource("ids", optionValueIds), Long.class);
    return result == null ? 0 : result;
  }

  @Override
  public boolean existsVariantOptionCombination(String productId, List<String> optionValueIds, String excludeVariantId) {
    String combinationKey = String.join("|", optionValueIds);
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
      WITH combinations AS (
        SELECT v.id,
               COALESCE(string_agg(vo.option_value_id, '|' ORDER BY vo.option_value_id), '') AS combo
        FROM variants v
        LEFT JOIN variant_options vo ON vo.variant_id = v.id
        WHERE v.product_id = :productId
          AND (CAST(:excludeVariantId AS VARCHAR) IS NULL OR v.id <> CAST(:excludeVariantId AS VARCHAR))
        GROUP BY v.id
      )
      SELECT EXISTS(SELECT 1 FROM combinations WHERE combo = :combo)
      """, new MapSqlParameterSource()
      .addValue("productId", productId)
      .addValue("excludeVariantId", excludeVariantId)
      .addValue("combo", combinationKey), Boolean.class));
  }

  @Override
  public Page<OptionListItemResponse> findOptions(Boolean active, AdminPageRequest pageRequest) {
    String where = active == null ? "" : "WHERE COALESCE(o.is_active, TRUE) = :active";
    MapSqlParameterSource params = new MapSqlParameterSource()
      .addValue("active", active)
      .addValue("limit", pageRequest.size())
      .addValue("offset", pageRequest.page() * pageRequest.size());

    List<OptionListItemResponse> content = jdbcTemplate.query("""
      SELECT o.id,
             o.code,
             o.name,
             COALESCE(o.is_active, TRUE) AS is_active,
             COALESCE(value_stats.value_count, 0) AS value_count,
             COALESCE(value_stats.usage_count, 0) AS usage_count
      FROM options o
      LEFT JOIN LATERAL (
        SELECT COUNT(DISTINCT ov.id) AS value_count,
               COUNT(DISTINCT vo.id) AS usage_count
        FROM option_values ov
        LEFT JOIN variant_options vo ON vo.option_value_id = ov.id
        WHERE ov.option_id = o.id
      ) value_stats ON TRUE
      """ + where
      + " ORDER BY " + optionOrderBy(pageRequest)
      + " LIMIT :limit OFFSET :offset", params, (rs, rowNum) -> new OptionListItemResponse(
      rs.getString("id"),
      rs.getString("code"),
      rs.getString("name"),
      rs.getBoolean("is_active"),
      rs.getLong("value_count"),
      rs.getLong("usage_count")
    ));

    Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM options o " + where, params, Long.class);
    return new PageImpl<>(content, PageRequest.of(pageRequest.page(), pageRequest.size()), total == null ? 0 : total);
  }

  @Override
  public Page<OptionValueListItemResponse> findOptionValues(String optionId, Boolean active, AdminPageRequest pageRequest) {
    StringBuilder where = new StringBuilder("WHERE 1=1 ");
    if (optionId != null && !optionId.isBlank()) {
      where.append("AND ov.option_id = :optionId ");
    }
    if (active != null) {
      where.append("AND ov.is_active = :active ");
    }
    MapSqlParameterSource params = new MapSqlParameterSource()
      .addValue("optionId", optionId)
      .addValue("active", active)
      .addValue("limit", pageRequest.size())
      .addValue("offset", pageRequest.page() * pageRequest.size());

    List<OptionValueListItemResponse> content = jdbcTemplate.query("""
      SELECT ov.id,
             ov.option_id,
             ov.value,
             ov.sort_order,
             ov.is_active,
             COUNT(vo.id) AS usage_count
      FROM option_values ov
      LEFT JOIN variant_options vo ON vo.option_value_id = ov.id
      """ + where + """
      GROUP BY ov.id, ov.option_id, ov.value, ov.sort_order, ov.is_active
      """
      + " ORDER BY " + optionValueOrderBy(pageRequest)
      + " LIMIT :limit OFFSET :offset", params, (rs, rowNum) -> new OptionValueListItemResponse(
      rs.getString("id"),
      rs.getString("option_id"),
      rs.getString("value"),
      rs.getInt("sort_order"),
      rs.getBoolean("is_active"),
      rs.getLong("usage_count")
    ));

    Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM option_values ov " + where, params, Long.class);
    return new PageImpl<>(content, PageRequest.of(pageRequest.page(), pageRequest.size()), total == null ? 0 : total);
  }

  @Override
  public boolean existsOption(String optionId) {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
      SELECT EXISTS(SELECT 1 FROM options WHERE id = :optionId)
      """, new MapSqlParameterSource("optionId", optionId), Boolean.class));
  }

  @Override
  public boolean existsOptionCodeIgnoreCase(String code, String excludeOptionId) {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
      SELECT EXISTS(
        SELECT 1
        FROM options
        WHERE LOWER(code) = LOWER(:code)
          AND (CAST(:excludeOptionId AS VARCHAR) IS NULL OR id <> CAST(:excludeOptionId AS VARCHAR))
      )
      """, new MapSqlParameterSource()
      .addValue("code", code)
      .addValue("excludeOptionId", excludeOptionId), Boolean.class));
  }

  @Override
  public String insertOption(String code, String name, boolean active) {
    String optionId = UUID.randomUUID().toString();
    jdbcTemplate.update("""
      INSERT INTO options (id, code, name, is_active)
      VALUES (:id, :code, :name, :active)
      """, new MapSqlParameterSource()
      .addValue("id", optionId)
      .addValue("code", code)
      .addValue("name", name)
      .addValue("active", active));
    return optionId;
  }

  @Override
  public void updateOption(String optionId, String code, String name, boolean active) {
    jdbcTemplate.update("""
      UPDATE options
      SET code = :code,
          name = :name,
          is_active = :active
      WHERE id = :optionId
      """, new MapSqlParameterSource()
      .addValue("optionId", optionId)
      .addValue("code", code)
      .addValue("name", name)
      .addValue("active", active));
  }

  @Override
  public void updateOptionActive(String optionId, boolean active) {
    jdbcTemplate.update("""
      UPDATE options SET is_active = :active WHERE id = :optionId
      """, new MapSqlParameterSource().addValue("optionId", optionId).addValue("active", active));
  }

  @Override
  public boolean existsOptionValue(String optionValueId) {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
      SELECT EXISTS(SELECT 1 FROM option_values WHERE id = :optionValueId)
      """, new MapSqlParameterSource("optionValueId", optionValueId), Boolean.class));
  }

  @Override
  public Optional<OptionValueListItemResponse> findOptionValueById(String optionValueId) {
    List<OptionValueListItemResponse> rows = jdbcTemplate.query("""
      SELECT ov.id,
             ov.option_id,
             ov.value,
             ov.sort_order,
             ov.is_active,
             COUNT(vo.id) AS usage_count
      FROM option_values ov
      LEFT JOIN variant_options vo ON vo.option_value_id = ov.id
      WHERE ov.id = :optionValueId
      GROUP BY ov.id, ov.option_id, ov.value, ov.sort_order, ov.is_active
      """, new MapSqlParameterSource("optionValueId", optionValueId), (rs, rowNum) -> new OptionValueListItemResponse(
      rs.getString("id"),
      rs.getString("option_id"),
      rs.getString("value"),
      rs.getInt("sort_order"),
      rs.getBoolean("is_active"),
      rs.getLong("usage_count")
    ));
    return rows.stream().findFirst();
  }

  @Override
  public boolean existsOptionValueForOptionIgnoreCase(String optionId, String value, String excludeOptionValueId) {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
      SELECT EXISTS(
        SELECT 1
        FROM option_values
        WHERE option_id = :optionId
          AND LOWER(value) = LOWER(:value)
          AND (CAST(:excludeOptionValueId AS VARCHAR) IS NULL OR id <> CAST(:excludeOptionValueId AS VARCHAR))
      )
      """, new MapSqlParameterSource()
      .addValue("optionId", optionId)
      .addValue("value", value)
      .addValue("excludeOptionValueId", excludeOptionValueId), Boolean.class));
  }

  @Override
  public String insertOptionValue(String optionId, String value, int sortOrder, boolean active) {
    String optionValueId = UUID.randomUUID().toString();
    jdbcTemplate.update("""
      INSERT INTO option_values (id, option_id, value, is_active, sort_order)
      VALUES (:id, :optionId, :value, :active, :sortOrder)
      """, new MapSqlParameterSource()
      .addValue("id", optionValueId)
      .addValue("optionId", optionId)
      .addValue("value", value)
      .addValue("active", active)
      .addValue("sortOrder", sortOrder));
    return optionValueId;
  }

  @Override
  public void updateOptionValue(String optionValueId, String value, int sortOrder, boolean active) {
    jdbcTemplate.update("""
      UPDATE option_values
      SET value = :value,
          sort_order = :sortOrder,
          is_active = :active
      WHERE id = :optionValueId
      """, new MapSqlParameterSource()
      .addValue("optionValueId", optionValueId)
      .addValue("value", value)
      .addValue("sortOrder", sortOrder)
      .addValue("active", active));
  }

  @Override
  public void updateOptionValueActive(String optionValueId, boolean active) {
    jdbcTemplate.update("""
      UPDATE option_values SET is_active = :active WHERE id = :optionValueId
      """, new MapSqlParameterSource().addValue("optionValueId", optionValueId).addValue("active", active));
  }

  @Override
  public long countActiveVariantAssignmentsForOptionValue(String optionValueId) {
    Long result = jdbcTemplate.queryForObject("""
      SELECT COUNT(*)
      FROM variant_options vo
      JOIN variants v ON v.id = vo.variant_id
      JOIN products p ON p.id = v.product_id
      WHERE vo.option_value_id = :optionValueId
        AND v.is_active = TRUE
        AND p.is_active = TRUE
      """, new MapSqlParameterSource("optionValueId", optionValueId), Long.class);
    return result == null ? 0 : result;
  }

  @Override
  public long countActiveVariantAssignmentsForOption(String optionId) {
    Long result = jdbcTemplate.queryForObject("""
      SELECT COUNT(*)
      FROM variant_options vo
      JOIN option_values ov ON ov.id = vo.option_value_id
      JOIN variants v ON v.id = vo.variant_id
      JOIN products p ON p.id = v.product_id
      WHERE ov.option_id = :optionId
        AND v.is_active = TRUE
        AND p.is_active = TRUE
      """, new MapSqlParameterSource("optionId", optionId), Long.class);
    return result == null ? 0 : result;
  }

  @Override
  public Page<SemanticMasterListItemResponse> findSemanticMasters(SemanticMasterKind kind, Boolean active, AdminPageRequest pageRequest) {
    String where = active == null ? "" : "WHERE COALESCE(m.is_active, TRUE) = :active";
    MapSqlParameterSource params = new MapSqlParameterSource()
      .addValue("active", active)
      .addValue("limit", pageRequest.size())
      .addValue("offset", pageRequest.page() * pageRequest.size());

    String table = masterTable(kind);
    String sql = """
      SELECT m.id,
             m.code,
             m.name,
      %s
             COALESCE(m.is_active, TRUE) AS is_active,
             %s AS usage_count
      FROM %s m
      %s
      ORDER BY %s
      LIMIT :limit OFFSET :offset
      """.formatted(
      semanticSelectColumns(kind),
      semanticUsageSql(kind, "m"),
      table,
      where,
      semanticOrderBy(pageRequest)
    );

    List<SemanticMasterListItemResponse> content = jdbcTemplate.query(sql, params, (rs, rowNum) -> new SemanticMasterListItemResponse(
      rs.getString("id"),
      rs.getString("code"),
      rs.getString("name"),
      rs.getString("slug"),
      rs.getString("description"),
      rs.getString("normalized_name"),
      rs.getString("inci_name"),
      rs.getString("logo_url"),
      rs.getBoolean("is_active"),
      rs.getLong("usage_count")
    ));

    Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table + " m " + where, params, Long.class);
    return new PageImpl<>(content, PageRequest.of(pageRequest.page(), pageRequest.size()), total == null ? 0 : total);
  }

  @Override
  public boolean existsSemanticMaster(SemanticMasterKind kind, String id) {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
      "SELECT EXISTS(SELECT 1 FROM " + masterTable(kind) + " WHERE id = :id)",
      new MapSqlParameterSource("id", id),
      Boolean.class
    ));
  }

  @Override
  public long countSemanticMasters(SemanticMasterKind kind, List<String> ids) {
    if (ids == null || ids.isEmpty()) {
      return 0;
    }
    Long result = jdbcTemplate.queryForObject(
      "SELECT COUNT(*) FROM " + masterTable(kind) + " WHERE id IN (:ids)",
      new MapSqlParameterSource("ids", ids),
      Long.class
    );
    return result == null ? 0 : result;
  }

  @Override
  public boolean existsSemanticMasterCodeIgnoreCase(SemanticMasterKind kind, String code, String excludeId) {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
      "SELECT EXISTS(SELECT 1 FROM " + masterTable(kind)
        + " WHERE LOWER(code) = LOWER(:code) AND (CAST(:excludeId AS VARCHAR) IS NULL OR id <> CAST(:excludeId AS VARCHAR)))",
      new MapSqlParameterSource().addValue("code", code).addValue("excludeId", excludeId),
      Boolean.class
    ));
  }

  @Override
  public boolean existsBrandSlugIgnoreCase(String slug, String excludeId) {
    return Boolean.TRUE.equals(jdbcTemplate.queryForObject("""
      SELECT EXISTS(
        SELECT 1
        FROM brands
        WHERE LOWER(slug) = LOWER(:slug)
          AND (CAST(:excludeId AS VARCHAR) IS NULL OR id <> CAST(:excludeId AS VARCHAR))
      )
      """, new MapSqlParameterSource().addValue("slug", slug).addValue("excludeId", excludeId), Boolean.class));
  }

  @Override
  public String insertBrand(String code, String name, String slug, String description, String logoUrl, boolean active) {
    String id = UUID.randomUUID().toString();
    jdbcTemplate.update("""
      INSERT INTO brands (id, code, name, slug, description, logo_url, is_active, created_at, updated_at)
      VALUES (:id, :code, :name, :slug, :description, :logoUrl, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
      """, new MapSqlParameterSource()
      .addValue("id", id)
      .addValue("code", code)
      .addValue("name", name)
      .addValue("slug", slug)
      .addValue("description", description)
      .addValue("logoUrl", logoUrl)
      .addValue("active", active));
    return id;
  }

  @Override
  public void updateBrand(String id, String code, String name, String slug, String description, String logoUrl, boolean active) {
    jdbcTemplate.update("""
      UPDATE brands
      SET code = :code,
          name = :name,
          slug = :slug,
          description = :description,
          logo_url = :logoUrl,
          is_active = :active,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = :id
      """, new MapSqlParameterSource()
      .addValue("id", id)
      .addValue("code", code)
      .addValue("name", name)
      .addValue("slug", slug)
      .addValue("description", description)
      .addValue("logoUrl", logoUrl)
      .addValue("active", active));
  }

  @Override
  public void updateBrandActive(String id, boolean active) {
    jdbcTemplate.update("""
      UPDATE brands SET is_active = :active, updated_at = CURRENT_TIMESTAMP WHERE id = :id
      """, new MapSqlParameterSource().addValue("id", id).addValue("active", active));
  }

  @Override
  public String insertIngredient(String code, String name, String normalizedName, String inciName, String description, boolean active) {
    String id = UUID.randomUUID().toString();
    jdbcTemplate.update("""
      INSERT INTO ingredients (id, code, name, normalized_name, inci_name, description, is_active, created_at, updated_at)
      VALUES (:id, :code, :name, :normalizedName, :inciName, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
      """, new MapSqlParameterSource()
      .addValue("id", id)
      .addValue("code", code)
      .addValue("name", name)
      .addValue("normalizedName", normalizedName)
      .addValue("inciName", inciName)
      .addValue("description", description)
      .addValue("active", active));
    return id;
  }

  @Override
  public void updateIngredient(String id, String code, String name, String normalizedName, String inciName, String description, boolean active) {
    jdbcTemplate.update("""
      UPDATE ingredients
      SET code = :code,
          name = :name,
          normalized_name = :normalizedName,
          inci_name = :inciName,
          description = :description,
          is_active = :active,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = :id
      """, new MapSqlParameterSource()
      .addValue("id", id)
      .addValue("code", code)
      .addValue("name", name)
      .addValue("normalizedName", normalizedName)
      .addValue("inciName", inciName)
      .addValue("description", description)
      .addValue("active", active));
  }

  @Override
  public void updateIngredientActive(String id, boolean active) {
    jdbcTemplate.update("""
      UPDATE ingredients SET is_active = :active, updated_at = CURRENT_TIMESTAMP WHERE id = :id
      """, new MapSqlParameterSource().addValue("id", id).addValue("active", active));
  }

  @Override
  public String insertSemanticMaster(SemanticMasterKind kind, String code, String name, String description, boolean active) {
    String id = UUID.randomUUID().toString();
    jdbcTemplate.update(
      "INSERT INTO " + masterTable(kind) + " (id, code, name, description, is_active, created_at, updated_at) "
        + "VALUES (:id, :code, :name, :description, :active, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
      new MapSqlParameterSource()
        .addValue("id", id)
        .addValue("code", code)
        .addValue("name", name)
        .addValue("description", description)
        .addValue("active", active)
    );
    return id;
  }

  @Override
  public void updateSemanticMaster(SemanticMasterKind kind, String id, String code, String name, String description, boolean active) {
    jdbcTemplate.update(
      "UPDATE " + masterTable(kind)
        + " SET code = :code, name = :name, description = :description, is_active = :active, updated_at = CURRENT_TIMESTAMP "
        + "WHERE id = :id",
      new MapSqlParameterSource()
        .addValue("id", id)
        .addValue("code", code)
        .addValue("name", name)
        .addValue("description", description)
        .addValue("active", active)
    );
  }

  @Override
  public void updateSemanticMasterActive(SemanticMasterKind kind, String id, boolean active) {
    jdbcTemplate.update(
      "UPDATE " + masterTable(kind) + " SET is_active = :active, updated_at = CURRENT_TIMESTAMP WHERE id = :id",
      new MapSqlParameterSource().addValue("id", id).addValue("active", active)
    );
  }

  @Override
  public void replaceProductSemanticReferences(String productId, SemanticMasterKind kind, List<String> referenceIds) {
    String table = relationTable(kind);
    String column = relationMasterIdColumn(kind);
    jdbcTemplate.update("DELETE FROM " + table + " WHERE product_id = :productId", new MapSqlParameterSource("productId", productId));
    for (String referenceId : referenceIds) {
      jdbcTemplate.update(
        "INSERT INTO " + table + " (id, product_id, " + column + ", created_at, updated_at) "
          + "VALUES (:id, :productId, :referenceId, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
        new MapSqlParameterSource()
          .addValue("id", UUID.randomUUID().toString())
          .addValue("productId", productId)
          .addValue("referenceId", referenceId)
      );
    }
  }

  @Override
  public List<SemanticReferenceResponse> findProductSemanticReferences(String productId, SemanticMasterKind kind) {
    String relationTable = relationTable(kind);
    String masterTable = masterTable(kind);
    String masterIdColumn = relationMasterIdColumn(kind);
    return jdbcTemplate.query(
      "SELECT m.id, m.code, m.name FROM " + relationTable + " r "
        + "JOIN " + masterTable + " m ON m.id = r." + masterIdColumn + " "
        + "WHERE r.product_id = :productId ORDER BY m.name ASC, m.id ASC",
      new MapSqlParameterSource("productId", productId),
      (rs, rowNum) -> new SemanticReferenceResponse(rs.getString("id"), rs.getString("code"), rs.getString("name"))
    );
  }

  @Override
  public List<String> findProductIdsBySemanticMaster(String masterId, SemanticMasterKind kind) {
    if (kind == SemanticMasterKind.BRAND) {
      return jdbcTemplate.query("""
        SELECT id
        FROM products
        WHERE brand_id = :masterId
          AND is_active = TRUE
        ORDER BY id ASC
        """, new MapSqlParameterSource("masterId", masterId), (rs, rowNum) -> rs.getString("id"));
    }
    return jdbcTemplate.query(
      "SELECT p.id FROM " + relationTable(kind) + " r "
        + "JOIN products p ON p.id = r.product_id "
        + "WHERE r." + relationMasterIdColumn(kind) + " = :masterId "
        + "AND p.is_active = TRUE ORDER BY p.id ASC",
      new MapSqlParameterSource("masterId", masterId),
      (rs, rowNum) -> rs.getString("id")
    );
  }

  @Override
  public CatalogHealthResponse findCatalogHealth() {
    List<CatalogHealthIssueCountResponse> issues = List.of(
      new CatalogHealthIssueCountResponse("PRODUCT_MISSING_PRIMARY_MEDIA", count("""
        SELECT COUNT(*)
        FROM products p
        WHERE NOT EXISTS (
          SELECT 1 FROM media m WHERE m.product_id = p.id AND m.variant_id IS NULL AND m.is_primary = TRUE
        )
        """)),
      new CatalogHealthIssueCountResponse("PRODUCT_WITHOUT_ACTIVE_VARIANT", count("""
        SELECT COUNT(*)
        FROM products p
        WHERE NOT EXISTS (
          SELECT 1 FROM variants v WHERE v.product_id = p.id AND v.is_active = TRUE
        )
        """)),
      new CatalogHealthIssueCountResponse("CUSTOMIZABLE_PRODUCT_VARIANT_WITHOUT_OPTIONS", count("""
        SELECT COUNT(DISTINCT v.id)
        FROM variants v
        JOIN products p ON p.id = v.product_id
        WHERE p.is_customizable = TRUE
          AND NOT EXISTS (SELECT 1 FROM variant_options vo WHERE vo.variant_id = v.id)
        """)),
      new CatalogHealthIssueCountResponse("PRODUCT_WITHOUT_DESCRIPTION", count("""
        SELECT COUNT(*)
        FROM products
        WHERE description_md IS NULL OR LENGTH(TRIM(description_md)) = 0
        """)),
      new CatalogHealthIssueCountResponse("IN_STOCK_BUT_INACTIVE_PRODUCT", count("""
        SELECT COUNT(*)
        FROM products p
        WHERE p.is_active = FALSE
          AND EXISTS (SELECT 1 FROM variants v WHERE v.product_id = p.id AND v.stock_quantity > 0)
        """)),
      new CatalogHealthIssueCountResponse("ACTIVE_PRODUCT_MISSING_PROJECTION", count("""
        SELECT COUNT(*)
        FROM products p
        LEFT JOIN product_search_documents d ON d.product_id = p.id
        WHERE p.is_active = TRUE AND d.product_id IS NULL
        """)),
      new CatalogHealthIssueCountResponse("STALE_PRODUCT_PROJECTION", count("""
        SELECT COUNT(*)
        FROM products p
        JOIN product_search_documents d ON d.product_id = p.id
        WHERE d.source_updated_at < """ + sourceUpdatedAtSql("p")))
    );
    return new CatalogHealthResponse(issues);
  }

  private List<VariantSummaryResponse> findVariantSummaries(String productId) {
    Map<String, VariantSummaryResponseBuilder> builders = new LinkedHashMap<>();
    jdbcTemplate.query("""
      SELECT v.id,
             v.sku,
             v.price,
             v.stock_quantity,
             v.is_active,
             ov.option_id,
             ov.id AS option_value_id,
             ov.value AS option_value_label
      FROM variants v
      LEFT JOIN variant_options vo ON vo.variant_id = v.id
      LEFT JOIN option_values ov ON ov.id = vo.option_value_id
      WHERE v.product_id = :productId
      ORDER BY v.created_at ASC, ov.sort_order ASC, ov.value ASC
      """, new MapSqlParameterSource("productId", productId), rs -> {
      String variantId = rs.getString("id");
      VariantSummaryResponseBuilder builder = builders.get(variantId);
      if (builder == null) {
        builder = new VariantSummaryResponseBuilder(
          variantId,
          rs.getString("sku"),
          SearchNormalizer.normalizeSku(rs.getString("sku")),
          rs.getBigDecimal("price"),
          rs.getInt("stock_quantity"),
          rs.getBoolean("is_active")
        );
        builders.put(variantId, builder);
      }
      if (rs.getString("option_value_id") != null) {
        builder.options().add(new OptionAssignmentResponse(
          rs.getString("option_id"),
          rs.getString("option_value_id"),
          rs.getString("option_value_label")
        ));
      }
    });
    return builders.values().stream().map(VariantSummaryResponseBuilder::build).toList();
  }

  private String buildProductWhereClause(String query, String typeId, Boolean active) {
    StringBuilder where = new StringBuilder("WHERE 1=1 ");
    if (query != null && !query.isBlank()) {
      where.append("AND (LOWER(p.name) LIKE :queryLike OR LOWER(p.slug) LIKE :queryLike) ");
    }
    if (typeId != null && !typeId.isBlank()) {
      where.append("AND p.type_id = :typeId ");
    }
    if (active != null) {
      where.append("AND p.is_active = :active ");
    }
    return where.toString();
  }

  private String productOrderBy(AdminPageRequest pageRequest) {
    return switch (pageRequest.sortField()) {
      case "name" -> "p.name " + pageRequest.sortDirection().name() + ", p.id ASC";
      case "slug" -> "p.slug " + pageRequest.sortDirection().name() + ", p.id ASC";
      case "projectionUpdatedAt" -> "d.projection_updated_at " + pageRequest.sortDirection().name() + " NULLS LAST, p.id ASC";
      default -> "p.updated_at " + pageRequest.sortDirection().name() + " NULLS LAST, p.id ASC";
    };
  }

  private String optionOrderBy(AdminPageRequest pageRequest) {
    return switch (pageRequest.sortField()) {
      case "code" -> "o.code " + pageRequest.sortDirection().name() + ", o.id ASC";
      case "usageCount" -> "usage_count " + pageRequest.sortDirection().name() + ", o.id ASC";
      default -> "o.name " + pageRequest.sortDirection().name() + ", o.id ASC";
    };
  }

  private String optionValueOrderBy(AdminPageRequest pageRequest) {
    return switch (pageRequest.sortField()) {
      case "value" -> "ov.value " + pageRequest.sortDirection().name() + ", ov.id ASC";
      case "usageCount" -> "usage_count " + pageRequest.sortDirection().name() + ", ov.id ASC";
      default -> "ov.sort_order " + pageRequest.sortDirection().name() + ", ov.id ASC";
    };
  }

  private String semanticOrderBy(AdminPageRequest pageRequest) {
    return switch (pageRequest.sortField()) {
      case "code" -> "m.code " + pageRequest.sortDirection().name() + ", m.id ASC";
      case "usageCount" -> "usage_count " + pageRequest.sortDirection().name() + ", m.id ASC";
      default -> "m.name " + pageRequest.sortDirection().name() + ", m.id ASC";
    };
  }

  private String masterTable(SemanticMasterKind kind) {
    return switch (kind) {
      case BRAND -> "brands";
      case INGREDIENT -> "ingredients";
      case SKIN_TYPE -> "skin_types";
      case CONCERN -> "concerns";
      case TAG -> "tags";
    };
  }

  private String relationTable(SemanticMasterKind kind) {
    return switch (kind) {
      case INGREDIENT -> "product_ingredients";
      case SKIN_TYPE -> "product_skin_types";
      case CONCERN -> "product_concerns";
      case TAG -> "product_tags";
      default -> throw new IllegalArgumentException("No relation table for " + kind);
    };
  }

  private String relationMasterIdColumn(SemanticMasterKind kind) {
    return switch (kind) {
      case INGREDIENT -> "ingredient_id";
      case SKIN_TYPE -> "skin_type_id";
      case CONCERN -> "concern_id";
      case TAG -> "tag_id";
      default -> throw new IllegalArgumentException("No relation column for " + kind);
    };
  }

  private String semanticSelectColumns(SemanticMasterKind kind) {
    return switch (kind) {
      case BRAND -> "m.slug, m.description, NULL AS normalized_name, NULL AS inci_name, m.logo_url, ";
      case INGREDIENT -> "NULL AS slug, m.description, m.normalized_name, m.inci_name, NULL AS logo_url, ";
      default -> "NULL AS slug, m.description, NULL AS normalized_name, NULL AS inci_name, NULL AS logo_url, ";
    };
  }

  private String semanticUsageSql(SemanticMasterKind kind, String alias) {
    return switch (kind) {
      case BRAND -> "(SELECT COUNT(*) FROM products p WHERE p.brand_id = " + alias + ".id)";
      case INGREDIENT -> "(SELECT COUNT(*) FROM product_ingredients r WHERE r.ingredient_id = " + alias + ".id)";
      case SKIN_TYPE -> "(SELECT COUNT(*) FROM product_skin_types r WHERE r.skin_type_id = " + alias + ".id)";
      case CONCERN -> "(SELECT COUNT(*) FROM product_concerns r WHERE r.concern_id = " + alias + ".id)";
      case TAG -> "(SELECT COUNT(*) FROM product_tags r WHERE r.tag_id = " + alias + ".id)";
    };
  }

  private long count(String sql) {
    Long result = jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), Long.class);
    return result == null ? 0 : result;
  }

  private LocalDateTime timestampToLocalDateTime(Timestamp timestamp) {
    return timestamp == null ? null : timestamp.toLocalDateTime();
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

  private record VariantSummaryResponseBuilder(
    String id,
    String sku,
    String normalizedSku,
    BigDecimal price,
    int stockQuantity,
    boolean active,
    List<OptionAssignmentResponse> options
  ) {

    private VariantSummaryResponseBuilder(String id, String sku, String normalizedSku, BigDecimal price, int stockQuantity, boolean active) {
      this(id, sku, normalizedSku, price, stockQuantity, active, new ArrayList<>());
    }

    private VariantSummaryResponse build() {
      return new VariantSummaryResponse(id, sku, normalizedSku, price, stockQuantity, active, List.copyOf(options));
    }
  }
}
