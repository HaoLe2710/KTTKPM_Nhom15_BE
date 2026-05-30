package fit.iuh.kttkpm_nhom15_be.events.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.events.application.dto.admin.EventPublicationAdminDtos.EventPublicationDetailResponse;
import fit.iuh.kttkpm_nhom15_be.events.application.dto.admin.EventPublicationAdminDtos.EventPublicationRowResponse;
import fit.iuh.kttkpm_nhom15_be.events.application.dto.admin.EventPublicationAdminDtos.EventPublicationSummaryResponse;
import fit.iuh.kttkpm_nhom15_be.events.domain.repositories.EventPublicationAdminRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EventPublicationAdminRepositoryImpl implements EventPublicationAdminRepository {

  private static final String BASE_FROM = " FROM event_publication ep ";

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public EventPublicationSummaryResponse getSummary() {
    String sql = """
      SELECT
        COUNT(*) AS total,
        COUNT(*) FILTER (WHERE ep.completion_date IS NULL) AS outstanding,
        COUNT(*) FILTER (WHERE ep.completion_date IS NOT NULL) AS completed,
        COUNT(*) FILTER (WHERE UPPER(COALESCE(ep.status, '')) IN ('FAILED', 'ERROR')) AS failed,
        COUNT(*) FILTER (WHERE ep.status IS NULL OR ep.status = '') AS with_unknown_status,
        MAX(ep.publication_date) AS latest_publication_date
      FROM event_publication ep
      """;

    return jdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), (rs, rowNum) ->
      new EventPublicationSummaryResponse(
        rs.getLong("total"),
        rs.getLong("outstanding"),
        rs.getLong("completed"),
        rs.getLong("failed"),
        rs.getLong("with_unknown_status"),
        toOffsetDateTime(rs, "latest_publication_date")
      )
    );
  }

  @Override
  public Page<EventPublicationRowResponse> findAll(String status,
                                                   String listenerId,
                                                   String eventType,
                                                   Boolean outstandingOnly,
                                                   OffsetDateTime publishedFrom,
                                                   OffsetDateTime publishedTo,
                                                   AdminPageRequest pageRequest) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    String where = buildWhere(status, listenerId, eventType, outstandingOnly, publishedFrom, publishedTo, params);

    String sql = """
      SELECT ep.id,
             ep.event_type,
             ep.listener_id,
             ep.status,
             ep.completion_attempts,
             ep.publication_date,
             ep.completion_date,
             ep.last_resubmission_date,
             ep.serialized_event
      """ + BASE_FROM + where + " ORDER BY " + toOrderBy(pageRequest)
      + " LIMIT :limit OFFSET :offset";

    params.addValue("limit", pageRequest.size());
    params.addValue("offset", pageRequest.page() * pageRequest.size());

    List<EventPublicationRowResponse> content = jdbcTemplate.query(sql, params, rowMapper());

    Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) " + BASE_FROM + where, params, Long.class);
    long totalElements = total == null ? 0 : total;

    return new PageImpl<>(content, PageRequest.of(pageRequest.page(), pageRequest.size()), totalElements);
  }

  @Override
  public Optional<EventPublicationDetailResponse> findById(String id) {
    String sql = """
      SELECT ep.id,
             ep.event_type,
             ep.listener_id,
             ep.status,
             ep.completion_attempts,
             ep.publication_date,
             ep.completion_date,
             ep.last_resubmission_date,
             ep.serialized_event
      FROM event_publication ep
      WHERE ep.id = CAST(:id AS UUID)
      """;

    List<EventPublicationDetailResponse> rows = jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), (rs, rowNum) ->
      new EventPublicationDetailResponse(
        rs.getString("id"),
        rs.getString("event_type"),
        rs.getString("listener_id"),
        rs.getString("status"),
        getInteger(rs, "completion_attempts"),
        toOffsetDateTime(rs, "publication_date"),
        toOffsetDateTime(rs, "completion_date"),
        toOffsetDateTime(rs, "last_resubmission_date"),
        rs.getObject("completion_date") == null,
        rs.getString("serialized_event")
      )
    );

    return rows.stream().findFirst();
  }

  private RowMapper<EventPublicationRowResponse> rowMapper() {
    return (rs, rowNum) -> {
      String serializedEvent = rs.getString("serialized_event");
      return new EventPublicationRowResponse(
        rs.getString("id"),
        rs.getString("event_type"),
        rs.getString("listener_id"),
        rs.getString("status"),
        getInteger(rs, "completion_attempts"),
        toOffsetDateTime(rs, "publication_date"),
        toOffsetDateTime(rs, "completion_date"),
        toOffsetDateTime(rs, "last_resubmission_date"),
        rs.getObject("completion_date") == null,
        preview(serializedEvent)
      );
    };
  }

  private String buildWhere(String status,
                            String listenerId,
                            String eventType,
                            Boolean outstandingOnly,
                            OffsetDateTime publishedFrom,
                            OffsetDateTime publishedTo,
                            MapSqlParameterSource params) {
    StringBuilder where = new StringBuilder(" WHERE 1=1 ");

    if (status != null && !status.isBlank()) {
      where.append(" AND UPPER(COALESCE(ep.status, '')) = UPPER(:status) ");
      params.addValue("status", status.trim());
    }

    if (listenerId != null && !listenerId.isBlank()) {
      where.append(" AND ep.listener_id ILIKE :listenerId ");
      params.addValue("listenerId", "%" + listenerId.trim() + "%");
    }

    if (eventType != null && !eventType.isBlank()) {
      where.append(" AND ep.event_type ILIKE :eventType ");
      params.addValue("eventType", "%" + eventType.trim() + "%");
    }

    if (Boolean.TRUE.equals(outstandingOnly)) {
      where.append(" AND ep.completion_date IS NULL ");
    }

    if (publishedFrom != null) {
      where.append(" AND ep.publication_date >= :publishedFrom ");
      params.addValue("publishedFrom", publishedFrom);
    }

    if (publishedTo != null) {
      where.append(" AND ep.publication_date <= :publishedTo ");
      params.addValue("publishedTo", publishedTo);
    }

    return where.toString();
  }

  private String toOrderBy(AdminPageRequest pageRequest) {
    String field = switch (pageRequest.sortField()) {
      case "completionDate" -> "ep.completion_date";
      case "completionAttempts" -> "ep.completion_attempts";
      case "status" -> "ep.status";
      case "listenerId" -> "ep.listener_id";
      case "eventType" -> "ep.event_type";
      default -> "ep.publication_date";
    };

    return field + " " + pageRequest.sortDirection().name() + ", ep.publication_date DESC";
  }

  private static OffsetDateTime toOffsetDateTime(ResultSet rs, String column) throws SQLException {
    return rs.getObject(column, OffsetDateTime.class);
  }

  private static Integer getInteger(ResultSet rs, String column) throws SQLException {
    Object value = rs.getObject(column);
    if (value == null) {
      return null;
    }
    return ((Number) value).intValue();
  }

  private static String preview(String raw) {
    if (raw == null) {
      return null;
    }
    if (raw.length() <= 240) {
      return raw;
    }
    return raw.substring(0, 240) + "...";
  }
}
