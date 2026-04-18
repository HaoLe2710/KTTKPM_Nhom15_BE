package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers.admin;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ToggleActiveRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.VariantCreateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.VariantStockPatchRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.VariantSummaryResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.VariantUpdateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.services.CatalogAdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Catalog")
@RestController
@RequestMapping("/api/v1/admin/variants")
@RequiredArgsConstructor
public class AdminVariantController {

  private final CatalogAdminService catalogAdminService;

  @PostMapping
  public ResponseEntity<VariantSummaryResponse> createVariant(@Valid @RequestBody VariantCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogAdminService.createVariant(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<VariantSummaryResponse> updateVariant(@PathVariable String id,
                                                              @Valid @RequestBody VariantUpdateRequest request) {
    return ResponseEntity.ok(catalogAdminService.updateVariant(id, request));
  }

  @PatchMapping("/{id}/stock")
  public ResponseEntity<Void> patchStock(@PathVariable String id,
                                         @Valid @RequestBody VariantStockPatchRequest request) {
    catalogAdminService.patchVariantStock(id, request);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/active")
  public ResponseEntity<Void> toggleActive(@PathVariable String id,
                                           @Valid @RequestBody ToggleActiveRequest request) {
    catalogAdminService.toggleVariantActive(id, request);
    return ResponseEntity.noContent().build();
  }
}
