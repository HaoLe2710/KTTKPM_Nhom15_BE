package fit.iuh.kttkpm_nhom15_be.search.presentation.controllers.admin;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionEnqueueResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SuggestionResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SuggestionWriteRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.services.SearchAdminService;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest.SortDirection;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.MessageResponse;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.support.AdminPageRequestFactory;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Search")
@RestController
@RequestMapping("/api/v1/admin/search/suggestions")
@RequiredArgsConstructor
public class AdminSearchSuggestionController {

  private static final Set<String> SUGGESTION_SORT_FIELDS = Set.of("weight", "keyword", "updatedAt");

  private final SearchAdminService searchAdminService;
  private final AdminPageRequestFactory adminPageRequestFactory;

  @GetMapping
  public ResponseEntity<Page<SuggestionResponse>> getSuggestions(@RequestParam(required = false) String locale,
                                                                 @RequestParam(required = false) Boolean active,
                                                                 @RequestParam(required = false) Integer page,
                                                                 @RequestParam(required = false) Integer size,
                                                                 @RequestParam(required = false) String sort) {
    return ResponseEntity.ok(searchAdminService.getSuggestions(
      locale,
      active,
      adminPageRequestFactory.create(page, size, sort, "weight", SortDirection.DESC, SUGGESTION_SORT_FIELDS)
    ));
  }

  @PostMapping
  public ResponseEntity<ProjectionEnqueueResponse> createSuggestion(@Valid @RequestBody SuggestionWriteRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(searchAdminService.createSuggestion(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<MessageResponse> updateSuggestion(@PathVariable String id,
                                                          @Valid @RequestBody SuggestionWriteRequest request) {
    searchAdminService.updateSuggestion(id, request);
    return ResponseEntity.ok(new MessageResponse("Gợi ý đã được cập nhật thành công"));
  }

  @PatchMapping("/{id}/active")
  public ResponseEntity<MessageResponse> toggleSuggestionActive(@PathVariable String id,
                                                                @RequestParam boolean active) {
    searchAdminService.toggleSuggestionActive(id, active);
    return ResponseEntity.ok(new MessageResponse("Trạng thái gợi ý đã được cập nhật thành công"));
  }
}
