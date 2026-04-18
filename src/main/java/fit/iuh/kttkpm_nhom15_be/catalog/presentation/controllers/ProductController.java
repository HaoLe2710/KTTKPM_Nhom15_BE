package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CompositeProductRequestDTO;
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
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ListProductsSummaryUseCase listProductsSummaryUseCase;
    private final GetProductDetailsUseCase getProductDetailsUseCase;
    private final CatalogAdminService catalogAdminService;

    @Operation(deprecated = true)
    @PostMapping
    public ResponseEntity<String> createProduct(@Valid @RequestBody CompositeProductRequestDTO request) {
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
        return ResponseEntity.ok(catalogAdminService.createProduct(adminRequest).id());
    }

    @Operation(deprecated = true)
    @GetMapping
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
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        return getProductDetailsUseCase.execute(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(deprecated = true)
    @PatchMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<Void> patchVariant(@PathVariable String productId,
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
        return ResponseEntity.ok().build();
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
