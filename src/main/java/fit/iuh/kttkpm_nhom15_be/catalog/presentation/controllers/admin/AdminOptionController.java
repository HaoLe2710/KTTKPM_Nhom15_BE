package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers.admin;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.CreatedResourceResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionValueListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionValueWriteRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionWriteRequest;
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
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminOptionController {

  private static final Set<String> OPTION_SORT_FIELDS = Set.of("name", "code", "usageCount");
  private static final Set<String> OPTION_VALUE_SORT_FIELDS = Set.of("value", "sortOrder", "usageCount");

  private final CatalogAdminService catalogAdminService;
  private final AdminPageRequestFactory adminPageRequestFactory;

  @GetMapping("/options")
  public ResponseEntity<Page<OptionListItemResponse>> getOptions(@RequestParam(required = false) Boolean active,
                                                                 @RequestParam(required = false) Integer page,
                                                                 @RequestParam(required = false) Integer size,
                                                                 @RequestParam(required = false) String sort) {
    return ResponseEntity.ok(catalogAdminService.getOptions(
      active,
      adminPageRequestFactory.create(page, size, sort, "name", SortDirection.ASC, OPTION_SORT_FIELDS)
    ));
  }

  @PostMapping("/options")
  public ResponseEntity<CreatedResourceResponse> createOption(@Valid @RequestBody OptionWriteRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogAdminService.createOption(request));
  }

  @PutMapping("/options/{id}")
  public ResponseEntity<MessageResponse> updateOption(@PathVariable String id,
                                                      @Valid @RequestBody OptionWriteRequest request) {
    catalogAdminService.updateOption(id, request);
    return ResponseEntity.ok(new MessageResponse("Tùy chọn đã được cập nhật thành công"));
  }

  @GetMapping("/option-values")
  public ResponseEntity<Page<OptionValueListItemResponse>> getOptionValues(@RequestParam(required = false) String optionId,
                                                                           @RequestParam(required = false) Boolean active,
                                                                           @RequestParam(required = false) Integer page,
                                                                           @RequestParam(required = false) Integer size,
                                                                           @RequestParam(required = false) String sort) {
    return ResponseEntity.ok(catalogAdminService.getOptionValues(
      optionId,
      active,
      adminPageRequestFactory.create(page, size, sort, "sortOrder", SortDirection.ASC, OPTION_VALUE_SORT_FIELDS)
    ));
  }

  @PostMapping("/options/{optionId}/values")
  public ResponseEntity<CreatedResourceResponse> createOptionValue(@PathVariable String optionId,
                                                                   @Valid @RequestBody OptionValueWriteRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogAdminService.createOptionValue(request, optionId));
  }

  @PutMapping("/option-values/{id}")
  public ResponseEntity<MessageResponse> updateOptionValue(@PathVariable String id,
                                                           @Valid @RequestBody OptionValueWriteRequest request) {
    catalogAdminService.updateOptionValue(id, request);
    return ResponseEntity.ok(new MessageResponse("Giá trị tùy chọn đã được cập nhật thành công"));
  }

  @PatchMapping("/option-values/{id}/active")
  public ResponseEntity<MessageResponse> toggleOptionValueActive(@PathVariable String id,
                                                                 @Valid @RequestBody ToggleActiveRequest request) {
    catalogAdminService.toggleOptionValueActive(id, request);
    return ResponseEntity.ok(new MessageResponse("Trạng thái giá trị tùy chọn đã được cập nhật thành công"));
  }
}
