package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers.admin;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.CatalogHealthResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.services.CatalogAdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Catalog")
@RestController
@RequestMapping("/api/v1/admin/catalog/health")
@RequiredArgsConstructor
public class AdminCatalogHealthController {

  private final CatalogAdminService catalogAdminService;

  @GetMapping
  public ResponseEntity<CatalogHealthResponse> getCatalogHealth() {
    return ResponseEntity.ok(catalogAdminService.getCatalogHealth());
  }
}
