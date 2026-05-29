package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers.admin;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.BrandWriteRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.CreatedResourceResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.IngredientWriteRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.SemanticMasterListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.SemanticMasterWriteRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ToggleActiveRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.services.CatalogAdminService;
import fit.iuh.kttkpm_nhom15_be.catalog.presentation.requests.admin.BrandMultipartWriteRequest;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Catalog")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminCatalogSemanticController {

  private static final Set<String> SORT_FIELDS = Set.of("name", "code", "usageCount");

  private final CatalogAdminService catalogAdminService;
  private final AdminPageRequestFactory adminPageRequestFactory;

  @GetMapping("/brands")
  public ResponseEntity<Page<SemanticMasterListItemResponse>> getBrands(@RequestParam(required = false) Boolean active,
                                                                        @RequestParam(required = false) Integer page,
                                                                        @RequestParam(required = false) Integer size,
                                                                        @RequestParam(required = false) String sort) {
    return ResponseEntity.ok(catalogAdminService.getBrands(
      active,
      adminPageRequestFactory.create(page, size, sort, "name", SortDirection.ASC, SORT_FIELDS)
    ));
  }

  @PostMapping("/brands")
  public ResponseEntity<CreatedResourceResponse> createBrand(@Valid @RequestBody BrandWriteRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogAdminService.createBrand(request));
  }

  @PostMapping(path = "/brands", consumes = "multipart/form-data")
  public ResponseEntity<CreatedResourceResponse> createBrandMultipart(@Valid @ModelAttribute BrandMultipartWriteRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogAdminService.createBrandWithLogo(request));
  }

  @PutMapping("/brands/{id}")
  public ResponseEntity<MessageResponse> updateBrand(@PathVariable String id, @Valid @RequestBody BrandWriteRequest request) {
    catalogAdminService.updateBrand(id, request);
    return ResponseEntity.ok(new MessageResponse("Thương hiệu đã được cập nhật thành công"));
  }

  @PutMapping(path = "/brands/{id}", consumes = "multipart/form-data")
  public ResponseEntity<Void> updateBrandMultipart(@PathVariable String id, @Valid @ModelAttribute BrandMultipartWriteRequest request) {
    catalogAdminService.updateBrandWithLogo(id, request);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/brands/{id}/active")
  public ResponseEntity<MessageResponse> toggleBrandActive(@PathVariable String id, @Valid @RequestBody ToggleActiveRequest request) {
    catalogAdminService.toggleBrandActive(id, request);
    return ResponseEntity.ok(new MessageResponse("Trạng thái thương hiệu đã được cập nhật thành công"));
  }

  @GetMapping("/ingredients")
  public ResponseEntity<Page<SemanticMasterListItemResponse>> getIngredients(@RequestParam(required = false) Boolean active,
                                                                             @RequestParam(required = false) Integer page,
                                                                             @RequestParam(required = false) Integer size,
                                                                             @RequestParam(required = false) String sort) {
    return ResponseEntity.ok(catalogAdminService.getIngredients(
      active,
      adminPageRequestFactory.create(page, size, sort, "name", SortDirection.ASC, SORT_FIELDS)
    ));
  }

  @PostMapping("/ingredients")
  public ResponseEntity<CreatedResourceResponse> createIngredient(@Valid @RequestBody IngredientWriteRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogAdminService.createIngredient(request));
  }

  @PutMapping("/ingredients/{id}")
  public ResponseEntity<MessageResponse> updateIngredient(@PathVariable String id, @Valid @RequestBody IngredientWriteRequest request) {
    catalogAdminService.updateIngredient(id, request);
    return ResponseEntity.ok(new MessageResponse("Thành phần đã được cập nhật thành công"));
  }

  @PatchMapping("/ingredients/{id}/active")
  public ResponseEntity<MessageResponse> toggleIngredientActive(@PathVariable String id, @Valid @RequestBody ToggleActiveRequest request) {
    catalogAdminService.toggleIngredientActive(id, request);
    return ResponseEntity.ok(new MessageResponse("Trạng thái thành phần đã được cập nhật thành công"));
  }

  @GetMapping("/skin-types")
  public ResponseEntity<Page<SemanticMasterListItemResponse>> getSkinTypes(@RequestParam(required = false) Boolean active,
                                                                           @RequestParam(required = false) Integer page,
                                                                           @RequestParam(required = false) Integer size,
                                                                           @RequestParam(required = false) String sort) {
    return ResponseEntity.ok(catalogAdminService.getSkinTypes(
      active,
      adminPageRequestFactory.create(page, size, sort, "name", SortDirection.ASC, SORT_FIELDS)
    ));
  }

  @PostMapping("/skin-types")
  public ResponseEntity<CreatedResourceResponse> createSkinType(@Valid @RequestBody SemanticMasterWriteRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogAdminService.createSkinType(request));
  }

  @PutMapping("/skin-types/{id}")
  public ResponseEntity<MessageResponse> updateSkinType(@PathVariable String id, @Valid @RequestBody SemanticMasterWriteRequest request) {
    catalogAdminService.updateSkinType(id, request);
    return ResponseEntity.ok(new MessageResponse("Loại da đã được cập nhật thành công"));
  }

  @PatchMapping("/skin-types/{id}/active")
  public ResponseEntity<MessageResponse> toggleSkinTypeActive(@PathVariable String id, @Valid @RequestBody ToggleActiveRequest request) {
    catalogAdminService.toggleSkinTypeActive(id, request);
    return ResponseEntity.ok(new MessageResponse("Trạng thái loại da đã được cập nhật thành công"));
  }

  @GetMapping("/concerns")
  public ResponseEntity<Page<SemanticMasterListItemResponse>> getConcerns(@RequestParam(required = false) Boolean active,
                                                                          @RequestParam(required = false) Integer page,
                                                                          @RequestParam(required = false) Integer size,
                                                                          @RequestParam(required = false) String sort) {
    return ResponseEntity.ok(catalogAdminService.getConcerns(
      active,
      adminPageRequestFactory.create(page, size, sort, "name", SortDirection.ASC, SORT_FIELDS)
    ));
  }

  @PostMapping("/concerns")
  public ResponseEntity<CreatedResourceResponse> createConcern(@Valid @RequestBody SemanticMasterWriteRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogAdminService.createConcern(request));
  }

  @PutMapping("/concerns/{id}")
  public ResponseEntity<MessageResponse> updateConcern(@PathVariable String id, @Valid @RequestBody SemanticMasterWriteRequest request) {
    catalogAdminService.updateConcern(id, request);
    return ResponseEntity.ok(new MessageResponse("Concern đã được cập nhật thành công"));
  }

  @PatchMapping("/concerns/{id}/active")
  public ResponseEntity<MessageResponse> toggleConcernActive(@PathVariable String id, @Valid @RequestBody ToggleActiveRequest request) {
    catalogAdminService.toggleConcernActive(id, request);
    return ResponseEntity.ok(new MessageResponse("Trạng thái concern đã được cập nhật thành công"));
  }

  @GetMapping("/tags")
  public ResponseEntity<Page<SemanticMasterListItemResponse>> getTags(@RequestParam(required = false) Boolean active,
                                                                      @RequestParam(required = false) Integer page,
                                                                      @RequestParam(required = false) Integer size,
                                                                      @RequestParam(required = false) String sort) {
    return ResponseEntity.ok(catalogAdminService.getTags(
      active,
      adminPageRequestFactory.create(page, size, sort, "name", SortDirection.ASC, SORT_FIELDS)
    ));
  }

  @PostMapping("/tags")
  public ResponseEntity<CreatedResourceResponse> createTag(@Valid @RequestBody SemanticMasterWriteRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogAdminService.createTag(request));
  }

  @PutMapping("/tags/{id}")
  public ResponseEntity<MessageResponse> updateTag(@PathVariable String id, @Valid @RequestBody SemanticMasterWriteRequest request) {
    catalogAdminService.updateTag(id, request);
    return ResponseEntity.ok(new MessageResponse("Tag đã được cập nhật thành công"));
  }

  @PatchMapping("/tags/{id}/active")
  public ResponseEntity<MessageResponse> toggleTagActive(@PathVariable String id, @Valid @RequestBody ToggleActiveRequest request) {
    catalogAdminService.toggleTagActive(id, request);
    return ResponseEntity.ok(new MessageResponse("Trạng thái tag đã được cập nhật thành công"));
  }
}
