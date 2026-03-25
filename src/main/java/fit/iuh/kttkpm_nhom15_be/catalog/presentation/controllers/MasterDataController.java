package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.*;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MasterDataController {

    private final CreateProductTypeUseCase createProductTypeUseCase;
    private final GetProductTypesUseCase getProductTypesUseCase;
    private final CreateOptionUseCase createOptionUseCase;
    private final GetOptionsUseCase getOptionsUseCase;

    @PostMapping("/product-types")
    public ResponseEntity<ProductTypeResponse> createProductType(@Valid @RequestBody ProductTypeRequest request) {
        return ResponseEntity.ok(createProductTypeUseCase.execute(request));
    }

    @GetMapping("/product-types")
    public ResponseEntity<List<ProductTypeResponse>> getProductTypes() {
        return ResponseEntity.ok(getProductTypesUseCase.execute());
    }

    @PostMapping("/options")
    public ResponseEntity<OptionResponse> createOption(@Valid @RequestBody OptionRequest request) {
        return ResponseEntity.ok(createOptionUseCase.execute(request));
    }

    @GetMapping("/options")
    public ResponseEntity<List<OptionResponse>> getOptions() {
        return ResponseEntity.ok(getOptionsUseCase.execute());
    }
}
