package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers.admin;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.CreatedResourceResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductCreateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductDetailResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductUpdateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.SearchStatusResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ToggleActiveRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.services.CatalogAdminService;
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

@Tag(name = "Admin Catalog")
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

  private static final Set<String> PRODUCT_SORT_FIELDS = Set.of("name", "slug", "updatedAt", "projectionUpdatedAt");

  private final CatalogAdminService catalogAdminService;
  private final AdminPageRequestFactory adminPageRequestFactory;

  @GetMapping
  public ResponseEntity<Page<ProductListItemResponse>> getProducts(@RequestParam(required = false) String q,
                                                                   @RequestParam(required = false) String typeId,
                                                                   @RequestParam(required = false) Boolean active,
                                                                   @RequestParam(required = false) Integer page,
                                                                   @RequestParam(required = false) Integer size,
                                                                   @RequestParam(required = false) String sort) {
    return ResponseEntity.ok(catalogAdminService.getProducts(
      q,
      typeId,
      active,
      adminPageRequestFactory.create(page, size, sort, "updatedAt", SortDirection.DESC, PRODUCT_SORT_FIELDS)
    ));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductDetailResponse> getProduct(@PathVariable String id) {
    return ResponseEntity.ok(catalogAdminService.getProductDetail(id));
  }

  @PostMapping
  public ResponseEntity<CreatedResourceResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogAdminService.createProduct(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductDetailResponse> updateProduct(@PathVariable String id,
                                                             @Valid @RequestBody ProductUpdateRequest request) {
    return ResponseEntity.ok(catalogAdminService.updateProduct(id, request));
  }

  @PatchMapping("/{id}/active")
  public ResponseEntity<MessageResponse> toggleProductActive(@PathVariable String id,
                                                             @Valid @RequestBody ToggleActiveRequest request) {
    catalogAdminService.toggleProductActive(id, request);
    return ResponseEntity.ok(new MessageResponse("Trạng thái sản phẩm đã được cập nhật thành công"));
  }

  @GetMapping("/{id}/search-status")
  public ResponseEntity<SearchStatusResponse> getSearchStatus(@PathVariable String id) {
    return ResponseEntity.ok(catalogAdminService.getSearchStatus(id));
  }
}
