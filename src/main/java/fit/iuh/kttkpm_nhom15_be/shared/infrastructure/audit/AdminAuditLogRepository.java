package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.audit;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminAuditLogRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public void insert(String actionType,
                     String resourceType,
                     String resourceId,
                     String actorRole,
                     String actorId,
                     String summaryPayload) {
    jdbcTemplate.update("""
      INSERT INTO admin_audit_logs (
        id, action_type, resource_type, resource_id, actor_role, actor_id, summary_payload, created_at
      ) VALUES (
        :id, :actionType, :resourceType, :resourceId, :actorRole, :actorId, :summaryPayload, :createdAt
      )
      """, new MapSqlParameterSource()
      .addValue("id", UUID.randomUUID().toString())
      .addValue("actionType", actionType)
      .addValue("resourceType", resourceType)
      .addValue("resourceId", resourceId)
      .addValue("actorRole", actorRole)
      .addValue("actorId", actorId)
      .addValue("summaryPayload", summaryPayload)
      .addValue("createdAt", Timestamp.valueOf(LocalDateTime.now())));
  }
}
