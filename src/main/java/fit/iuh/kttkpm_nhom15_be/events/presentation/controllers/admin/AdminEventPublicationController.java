package fit.iuh.kttkpm_nhom15_be.events.presentation.controllers.admin;

import fit.iuh.kttkpm_nhom15_be.events.application.dto.admin.EventPublicationAdminDtos.EventPublicationDetailResponse;
import fit.iuh.kttkpm_nhom15_be.events.application.dto.admin.EventPublicationAdminDtos.EventPublicationRowResponse;
import fit.iuh.kttkpm_nhom15_be.events.application.dto.admin.EventPublicationAdminDtos.EventPublicationSummaryResponse;
import fit.iuh.kttkpm_nhom15_be.events.application.services.EventPublicationAdminService;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest.SortDirection;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.support.AdminPageRequestFactory;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Catalog")
@RestController
@RequestMapping("/api/v1/admin/events/publications")
@RequiredArgsConstructor
public class AdminEventPublicationController {

  private static final Set<String> SORT_FIELDS = Set.of(
    "publicationDate",
    "completionDate",
    "completionAttempts",
    "status",
    "listenerId",
    "eventType"
  );

  private final EventPublicationAdminService service;
  private final AdminPageRequestFactory adminPageRequestFactory;

  @GetMapping("/summary")
  public ResponseEntity<EventPublicationSummaryResponse> getSummary() {
    return ResponseEntity.ok(service.getSummary());
  }

  @GetMapping
  public ResponseEntity<Page<EventPublicationRowResponse>> getPublications(
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String listenerId,
    @RequestParam(required = false) String eventType,
    @RequestParam(required = false) Boolean outstandingOnly,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime publishedFrom,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime publishedTo,
    @RequestParam(required = false) Integer page,
    @RequestParam(required = false) Integer size,
    @RequestParam(required = false) String sort
  ) {
    return ResponseEntity.ok(service.getPublications(
      status,
      listenerId,
      eventType,
      outstandingOnly,
      publishedFrom,
      publishedTo,
      adminPageRequestFactory.create(page, size, sort, "publicationDate", SortDirection.DESC, SORT_FIELDS)
    ));
  }

  @GetMapping("/{id}")
  public ResponseEntity<EventPublicationDetailResponse> getPublicationById(@PathVariable String id) {
    return ResponseEntity.ok(service.getPublicationById(id));
  }
}
