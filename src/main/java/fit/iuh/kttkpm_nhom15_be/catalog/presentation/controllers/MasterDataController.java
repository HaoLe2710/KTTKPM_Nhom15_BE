package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionValueWriteRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionWriteRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.OptionRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.OptionResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.OptionValueResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.ProductTypeRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.ProductTypeResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.services.CatalogAdminService;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.CreateOptionUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.CreateProductTypeUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.DeleteOptionUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.DeleteProductTypeUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.GetOptionsUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.GetProductTypesUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.UpdateOptionUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.UpdateProductTypeUseCase;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest.SortDirection;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(createProductTypeUseCase.execute(request));
    }

    @Operation(deprecated = true)
    @GetMapping("/product-types")
    public ResponseEntity<List<ProductTypeResponse>> getProductTypes() {
        return ResponseEntity.ok(getProductTypesUseCase.execute());
    }

    @Operation(deprecated = true)
    @PutMapping("/product-types/{id}")
    public ResponseEntity<ProductTypeResponse> updateProductType(@PathVariable String id,
                                                                 @Valid @RequestBody ProductTypeRequest request) {
        return ResponseEntity.ok(updateProductTypeUseCase.execute(id, request));
    }

    @Operation(deprecated = true)
    @DeleteMapping("/product-types/{id}")
    public ResponseEntity<MessageResponse> deleteProductType(@PathVariable String id) {
        deleteProductTypeUseCase.execute(id);
        return ResponseEntity.ok(new MessageResponse("Loại sản phẩm đã được xóa thành công"));
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
        return ResponseEntity.status(HttpStatus.CREATED).body(OptionResponse.builder()
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
    public ResponseEntity<MessageResponse> deleteOption(@PathVariable String id) {
        deleteOptionUseCase.execute(id);
        return ResponseEntity.ok(new MessageResponse("Tùy chọn đã được xóa thành công"));
    }
}
