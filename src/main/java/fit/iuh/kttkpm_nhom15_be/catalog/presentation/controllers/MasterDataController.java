package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.*;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.*;
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

    @PostMapping("/product-types")
    public ResponseEntity<ProductTypeResponse> createProductType(@Valid @RequestBody ProductTypeRequest request) {
        return ResponseEntity.ok(createProductTypeUseCase.execute(request));
    }

    @GetMapping("/product-types")
    public ResponseEntity<List<ProductTypeResponse>> getProductTypes() {
        return ResponseEntity.ok(getProductTypesUseCase.execute());
    }

    @PutMapping("/product-types/{id}")
    public ResponseEntity<ProductTypeResponse> updateProductType(@PathVariable String id,
                                                                 @Valid @RequestBody ProductTypeRequest request) {
        return ResponseEntity.ok(updateProductTypeUseCase.execute(id, request));
    }

    @DeleteMapping("/product-types/{id}")
    public ResponseEntity<Void> deleteProductType(@PathVariable String id) {
        deleteProductTypeUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/options")
    public ResponseEntity<OptionResponse> createOption(@Valid @RequestBody OptionRequest request) {
        return ResponseEntity.ok(createOptionUseCase.execute(request));
    }

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
