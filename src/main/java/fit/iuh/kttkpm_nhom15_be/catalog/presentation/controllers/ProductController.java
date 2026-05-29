package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CompositeProductRequestDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.PublicProductDetailResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.CreatedResourceResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductCreateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductVariantCreateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.VariantOptionAssignmentRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.VariantUpdateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.ProductSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.services.CatalogAdminService;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product.GetProductDetailsUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product.ListProductsSummaryUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product.UpdateVariantPricingUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Product;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiNotFoundException;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ListProductsSummaryUseCase listProductsSummaryUseCase;
    private final GetProductDetailsUseCase getProductDetailsUseCase;
    private final CatalogAdminService catalogAdminService;

    @Operation(deprecated = true)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CreatedResourceResponse> createProduct(@Valid @RequestBody CompositeProductRequestDTO request) {
        ProductCreateRequest adminRequest = new ProductCreateRequest(
                request.getTypeId(),
                request.getName(),
                null,
                request.getDescriptionMd(),
                null,
                null,
                request.isCustomizable(),
                true,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                request.getVariants().stream()
                        .map(variant -> new ProductVariantCreateRequest(
                                variant.getSku(),
                                variant.getPrice(),
                                variant.getStockQuantity(),
                                true,
                                mapOptions(variant.getOptions())
                        ))
                        .toList()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreatedResourceResponse(catalogAdminService.createProduct(adminRequest).id()));
    }

    @Operation(deprecated = true)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProductSummaryDTO>> getProducts(
            @RequestParam(required = false) String typeId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(listProductsSummaryUseCase.execute(typeId, minPrice, maxPrice, page, size));
    }

    @Operation(deprecated = true)
    @GetMapping("/{id}")
    public ResponseEntity<PublicProductDetailResponse> getProduct(@PathVariable String id) {
        Product product = getProductDetailsUseCase.execute(id)
                .orElseThrow(() -> new ApiNotFoundException("Không tìm thấy sản phẩm với id: " + id));
        var detail = catalogAdminService.getProductDetail(id);
        return ResponseEntity.ok(PublicProductDetailResponse.from(product, detail));
    }

    @Operation(deprecated = true)
    @PatchMapping("/{productId}/variants/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> patchVariant(@PathVariable String productId,
                                                        @PathVariable String variantId,
                                                        @Valid @RequestBody UpdateVariantPricingUseCase.PatchVariantRequest request) {
        var product = catalogAdminService.getProductDetail(productId);
        var variant = product.variants().stream()
                .filter(item -> item.id().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new ApiNotFoundException("Variant not found"));
        catalogAdminService.updateVariant(variantId, new VariantUpdateRequest(
                variant.sku(),
                request.getPrice(),
                variant.stockQuantity() + request.getAddedStock(),
                variant.active(),
                variant.options().stream()
                        .map(option -> new VariantOptionAssignmentRequest(option.optionId(), option.optionValueId()))
                        .toList()
        ));
        return ResponseEntity.ok(new MessageResponse("Biến thể sản phẩm đã được cập nhật thành công"));
    }

    private List<VariantOptionAssignmentRequest> mapOptions(List<CompositeProductRequestDTO.OptionAssignmentDTO> options) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }
        return options.stream()
                .map(option -> new VariantOptionAssignmentRequest(option.getOptionId(), option.getValueId()))
                .toList();
    }
}
