package fit.iuh.kttkpm_nhom15_be.search.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.search.application.results.TriggerFullSearchProjectionRebuildResult;
import fit.iuh.kttkpm_nhom15_be.search.application.services.SearchAdminService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchProjectionAdminController {

  private final SearchAdminService searchAdminService;

  @Operation(deprecated = true)
  @PostMapping("/api/v1/search/projections/rebuild-all")
  public ResponseEntity<TriggerFullSearchProjectionRebuildResult> rebuildAllProducts() {
    return ResponseEntity.accepted().body(searchAdminService.rebuildAll());
  }
}
