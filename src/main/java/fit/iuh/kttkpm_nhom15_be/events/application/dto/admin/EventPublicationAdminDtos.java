package fit.iuh.kttkpm_nhom15_be.events.application.dto.admin;

import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;

public final class EventPublicationAdminDtos {

  private EventPublicationAdminDtos() {
  }

  public record EventPublicationSummaryResponse(
    long total,
    long outstanding,
    long completed,
    long failed,
    long withUnknownStatus,
    OffsetDateTime latestPublicationDate
  ) {
  }

  public record EventPublicationRowResponse(
    String id,
    String eventType,
    String listenerId,
    String status,
    Integer completionAttempts,
    OffsetDateTime publicationDate,
    OffsetDateTime completionDate,
    OffsetDateTime lastResubmissionDate,
    boolean outstanding,
    String serializedEventPreview
  ) {
  }

  public record EventPublicationDetailResponse(
    String id,
    String eventType,
    String listenerId,
    String status,
    Integer completionAttempts,
    OffsetDateTime publicationDate,
    OffsetDateTime completionDate,
    OffsetDateTime lastResubmissionDate,
    boolean outstanding,
    String serializedEvent
  ) {
  }

  public record EventPublicationPageResponse(
    Page<EventPublicationRowResponse> page
  ) {
  }
}
