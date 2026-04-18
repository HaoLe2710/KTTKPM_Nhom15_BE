package fit.iuh.kttkpm_nhom15_be.search.presentation.controllers.admin;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionEnqueueResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SynonymGroupResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SynonymGroupWriteRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SynonymTermWriteRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.services.SearchAdminService;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest.SortDirection;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.support.AdminPageRequestFactory;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Search")
@RestController
@RequestMapping("/api/v1/admin/search")
@RequiredArgsConstructor
public class AdminSearchSynonymController {

  private static final Set<String> SYNONYM_SORT_FIELDS = Set.of("updatedAt", "code", "locale");

  private final SearchAdminService searchAdminService;
  private final AdminPageRequestFactory adminPageRequestFactory;

  @GetMapping("/synonym-groups")
  public ResponseEntity<Page<SynonymGroupResponse>> getSynonymGroups(@RequestParam(required = false) String locale,
                                                                     @RequestParam(required = false) Boolean active,
                                                                     @RequestParam(required = false) Integer page,
                                                                     @RequestParam(required = false) Integer size,
                                                                     @RequestParam(required = false) String sort) {
    return ResponseEntity.ok(searchAdminService.getSynonymGroups(
      locale,
      active,
      adminPageRequestFactory.create(page, size, sort, "updatedAt", SortDirection.DESC, SYNONYM_SORT_FIELDS)
    ));
  }

  @PostMapping("/synonym-groups")
  public ResponseEntity<ProjectionEnqueueResponse> createSynonymGroup(@Valid @RequestBody SynonymGroupWriteRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(searchAdminService.createSynonymGroup(request));
  }

  @PutMapping("/synonym-groups/{id}")
  public ResponseEntity<Void> updateSynonymGroup(@PathVariable String id,
                                                 @Valid @RequestBody SynonymGroupWriteRequest request) {
    searchAdminService.updateSynonymGroup(id, request);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/synonym-groups/{id}/terms")
  public ResponseEntity<ProjectionEnqueueResponse> addSynonymTerm(@PathVariable String id,
                                                                  @Valid @RequestBody SynonymTermWriteRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(searchAdminService.addSynonymTerm(id, request));
  }

  @DeleteMapping("/synonym-terms/{termId}")
  public ResponseEntity<Void> deleteSynonymTerm(@PathVariable String termId) {
    searchAdminService.deleteSynonymTerm(termId);
    return ResponseEntity.noContent().build();
  }
}
