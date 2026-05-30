package fit.iuh.kttkpm_nhom15_be.events.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.events.application.dto.admin.EventPublicationAdminDtos.EventPublicationDetailResponse;
import fit.iuh.kttkpm_nhom15_be.events.application.dto.admin.EventPublicationAdminDtos.EventPublicationRowResponse;
import fit.iuh.kttkpm_nhom15_be.events.application.dto.admin.EventPublicationAdminDtos.EventPublicationSummaryResponse;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface EventPublicationAdminRepository {

  EventPublicationSummaryResponse getSummary();

  Page<EventPublicationRowResponse> findAll(String status,
                                            String listenerId,
                                            String eventType,
                                            Boolean outstandingOnly,
                                            OffsetDateTime publishedFrom,
                                            OffsetDateTime publishedTo,
                                            AdminPageRequest pageRequest);

  Optional<EventPublicationDetailResponse> findById(String id);
}
