package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.*;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionValueWriteRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionWriteRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.services.CatalogAdminService;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.*;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest.SortDirection;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MasterDataController {

    private final CreateProductTypeUseCase createProductTypeUseCase;
    private final UpdateProductTypeUseCase updateProductTypeUseCase;
    private final DeleteProductTypeUseCase deleteProductTypeUseCase;
    private final GetProductTypesUseCase getProductTypesUseCase;
    private final CreateOptionUseCase createOptionUseCase;
    private final UpdateOptionUseCase updateOptionUseCase;
    private final DeleteOptionUseCase deleteOptionUseCase;
    private final GetOptionsUseCase getOptionsUseCase;
    private final CatalogAdminService catalogAdminService;

    @Operation(deprecated = true)
    @PostMapping("/product-types")
    public ResponseEntity<ProductTypeResponse> createProductType(@Valid @RequestBody ProductTypeRequest request) {
        return ResponseEntity.ok(createProductTypeUseCase.execute(request));
    }

    @Operation(deprecated = true)
    @GetMapping("/product-types")
    public ResponseEntity<List<ProductTypeResponse>> getProductTypes() {
        return ResponseEntity.ok(getProductTypesUseCase.execute());
    }

    @Operation(deprecated = true)
    @PostMapping("/options")
    public ResponseEntity<OptionResponse> createOption(@Valid @RequestBody OptionRequest request) {
        var created = catalogAdminService.createOption(new OptionWriteRequest(
                request.getCode(),
                request.getName(),
                true,
                request.getValues() == null ? List.of() : request.getValues().stream()
                        .map(value -> new OptionValueWriteRequest(value, null, true))
                        .toList()
        ));
        var values = catalogAdminService.getOptionValues(
                created.id(),
                null,
                new AdminPageRequest(0, 100, "sortOrder", SortDirection.ASC)
        );
        return ResponseEntity.ok(OptionResponse.builder()
                .id(created.id())
                .code(request.getCode())
                .name(request.getName())
                .values(values.getContent().stream()
                        .map(value -> OptionValueResponse.builder()
                                .id(value.id())
                                .value(value.value())
                                .build())
                        .toList())
                .build());
    }

    @Operation(deprecated = true)
    @GetMapping("/options")
    public ResponseEntity<List<OptionResponse>> getOptions() {
        return ResponseEntity.ok(getOptionsUseCase.execute());
    }

    @PutMapping("/options/{id}")
    public ResponseEntity<OptionResponse> updateOption(@PathVariable String id,
                                                       @Valid @RequestBody OptionRequest request) {
        return ResponseEntity.ok(updateOptionUseCase.execute(id, request));
    }

    @DeleteMapping("/options/{id}")
    public ResponseEntity<Void> deleteOption(@PathVariable String id) {
        deleteOptionUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(java.util.NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}
