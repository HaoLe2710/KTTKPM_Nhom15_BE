package fit.iuh.kttkpm_nhom15_be.search.presentation.controllers.admin;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.FailureResolveRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionEnqueueResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionFailureResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionRunResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionTaskResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.results.TriggerFullSearchProjectionRebuildResult;
import fit.iuh.kttkpm_nhom15_be.search.application.services.SearchAdminService;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest.SortDirection;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.MessageResponse;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.support.AdminPageRequestFactory;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Search")
@RestController
@RequestMapping("/api/v1/admin/search")
@RequiredArgsConstructor
public class AdminSearchProjectionController {

  private static final Set<String> TASK_SORT_FIELDS = Set.of("nextAttemptAt", "updatedAt", "productId", "status");
  private static final Set<String> FAILURE_SORT_FIELDS = Set.of("failedAt", "retryCount", "productId");
  private static final Set<String> RUN_SORT_FIELDS = Set.of("startedAt", "finishedAt", "processedCount");

  private final SearchAdminService searchAdminService;
  private final AdminPageRequestFactory adminPageRequestFactory;

  @PostMapping("/projections/rebuild/{productId}")
  public ResponseEntity<ProjectionEnqueueResponse> rebuildOne(@PathVariable String productId) {
    return ResponseEntity.accepted().body(searchAdminService.rebuildOne(productId));
  }

  @PostMapping("/projections/rebuild-all")
  public ResponseEntity<TriggerFullSearchProjectionRebuildResult> rebuildAll() {
    return ResponseEntity.accepted().body(searchAdminService.rebuildAll());
  }

  @GetMapping("/projection-tasks")
  public ResponseEntity<Page<ProjectionTaskResponse>> getTasks(@RequestParam(required = false) Integer page,
                                                               @RequestParam(required = false) Integer size,
                                                               @RequestParam(required = false) String sort) {
    return ResponseEntity.ok(searchAdminService.getProjectionTasks(
      adminPageRequestFactory.create(page, size, sort, "nextAttemptAt", SortDirection.ASC, TASK_SORT_FIELDS)
    ));
  }

  @GetMapping("/projection-failures")
  public ResponseEntity<Page<ProjectionFailureResponse>> getFailures(@RequestParam(required = false) String state,
                                                                     @RequestParam(required = false) Integer page,
                                                                     @RequestParam(required = false) Integer size,
                                                                     @RequestParam(required = false) String sort) {
    return ResponseEntity.ok(searchAdminService.getProjectionFailures(
      state,
      adminPageRequestFactory.create(page, size, sort, "failedAt", SortDirection.DESC, FAILURE_SORT_FIELDS)
    ));
  }

  @GetMapping("/projection-runs")
  public ResponseEntity<Page<ProjectionRunResponse>> getRuns(@RequestParam(required = false) Integer page,
                                                             @RequestParam(required = false) Integer size,
                                                             @RequestParam(required = false) String sort) {
    return ResponseEntity.ok(searchAdminService.getProjectionRuns(
      adminPageRequestFactory.create(page, size, sort, "startedAt", SortDirection.DESC, RUN_SORT_FIELDS)
    ));
  }

  @PostMapping("/projection-failures/{productId}/retry")
  public ResponseEntity<ProjectionEnqueueResponse> retryFailure(@PathVariable String productId) {
    return ResponseEntity.accepted().body(searchAdminService.retryFailure(productId));
  }

  @PostMapping("/projection-failures/{productId}/resolve")
  public ResponseEntity<MessageResponse> resolveFailure(@PathVariable String productId,
                                                        @RequestBody(required = false) @Valid FailureResolveRequest request) {
    searchAdminService.resolveFailure(productId, request);
    return ResponseEntity.ok(new MessageResponse("Projection failure da duoc xu ly thanh cong"));
  }
}
