package fit.iuh.kttkpm_nhom15_be.search.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.search.application.results.TriggerFullSearchProjectionRebuildResult;
import fit.iuh.kttkpm_nhom15_be.search.application.usecases.projection.TriggerFullSearchProjectionRebuildUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchProjectionAdminController {

  private final TriggerFullSearchProjectionRebuildUseCase triggerFullSearchProjectionRebuildUseCase;

  @PostMapping("/api/v1/search/projections/rebuild-all")
  public ResponseEntity<TriggerFullSearchProjectionRebuildResult> rebuildAllProducts() {
    return ResponseEntity.accepted().body(triggerFullSearchProjectionRebuildUseCase.execute());
  }
}
