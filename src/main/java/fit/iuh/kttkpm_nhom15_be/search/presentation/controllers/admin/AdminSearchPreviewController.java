package fit.iuh.kttkpm_nhom15_be.search.presentation.controllers.admin;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SearchPreviewResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.services.SearchAdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Search")
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminSearchPreviewController {

  private final SearchAdminService searchAdminService;

  @GetMapping("/{id}/search-preview")
  public ResponseEntity<SearchPreviewResponse> getSearchPreview(@PathVariable String id) {
    return ResponseEntity.ok(searchAdminService.getSearchPreview(id));
  }
}
