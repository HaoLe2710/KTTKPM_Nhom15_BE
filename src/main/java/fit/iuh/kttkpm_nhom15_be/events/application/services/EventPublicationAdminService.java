package fit.iuh.kttkpm_nhom15_be.events.application.services;

import fit.iuh.kttkpm_nhom15_be.events.application.dto.admin.EventPublicationAdminDtos.EventPublicationDetailResponse;
import fit.iuh.kttkpm_nhom15_be.events.application.dto.admin.EventPublicationAdminDtos.EventPublicationRowResponse;
import fit.iuh.kttkpm_nhom15_be.events.application.dto.admin.EventPublicationAdminDtos.EventPublicationSummaryResponse;
import fit.iuh.kttkpm_nhom15_be.events.domain.repositories.EventPublicationAdminRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiNotFoundException;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublicationAdminService {

  private final EventPublicationAdminRepository repository;

  public EventPublicationSummaryResponse getSummary() {
    return repository.getSummary();
  }

  public Page<EventPublicationRowResponse> getPublications(String status,
                                                           String listenerId,
                                                           String eventType,
                                                           Boolean outstandingOnly,
                                                           OffsetDateTime publishedFrom,
                                                           OffsetDateTime publishedTo,
                                                           AdminPageRequest pageRequest) {
    return repository.findAll(status, listenerId, eventType, outstandingOnly, publishedFrom, publishedTo, pageRequest);
  }

  public EventPublicationDetailResponse getPublicationById(String id) {
    return repository.findById(id)
      .orElseThrow(() -> new ApiNotFoundException("Event publication not found"));
  }
}
