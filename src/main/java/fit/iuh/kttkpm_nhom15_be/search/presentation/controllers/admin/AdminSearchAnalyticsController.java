package fit.iuh.kttkpm_nhom15_be.search.presentation.controllers.admin;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionHealthSummaryResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.TopClickedProductResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.TopQueryResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ZeroResultQueryResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.services.SearchAdminService;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest.SortDirection;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.support.AdminPageRequestFactory;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Search")
@RestController
@RequestMapping("/api/v1/admin/search")
@RequiredArgsConstructor
public class AdminSearchAnalyticsController {

  private static final Set<String> ZERO_RESULT_SORT_FIELDS = Set.of("occurrenceCount", "lastSeenAt");

  private final SearchAdminService searchAdminService;
  private final AdminPageRequestFactory adminPageRequestFactory;

  @GetMapping("/top-queries")
  public ResponseEntity<List<TopQueryResponse>> getTopQueries(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                              @RequestParam(required = false) String locale,
                                                              @RequestParam(defaultValue = "20") int limit) {
    return ResponseEntity.ok(searchAdminService.getTopQueries(from, to, locale, limit));
  }

  @GetMapping("/zero-result-queries")
  public ResponseEntity<Page<ZeroResultQueryResponse>> getZeroResultQueries(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                                            @RequestParam(required = false) String locale,
                                                                            @RequestParam(required = false) Integer page,
                                                                            @RequestParam(required = false) Integer size,
                                                                            @RequestParam(required = false) String sort) {
    return ResponseEntity.ok(searchAdminService.getZeroResultQueries(
      from,
      to,
      locale,
      adminPageRequestFactory.create(page, size, sort, "occurrenceCount", SortDirection.DESC, ZERO_RESULT_SORT_FIELDS)
    ));
  }

  @GetMapping("/top-clicked-products")
  public ResponseEntity<List<TopClickedProductResponse>> getTopClickedProducts(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                                               @RequestParam(required = false) String locale,
                                                                               @RequestParam(defaultValue = "20") int limit) {
    return ResponseEntity.ok(searchAdminService.getTopClickedProducts(from, to, locale, limit));
  }

  @GetMapping("/summary")
  public ResponseEntity<ProjectionHealthSummaryResponse> getSummary() {
    return ResponseEntity.ok(searchAdminService.getProjectionHealthSummary());
  }
}
