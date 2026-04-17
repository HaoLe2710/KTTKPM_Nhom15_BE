package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CompositeProductRequestDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.ProductSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product.CreateCompositeProductUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product.GetProductDetailsUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product.ListProductsSummaryUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product.UpdateVariantPricingUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Product;
import jakarta.validation.Valid;
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
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {

    private final CreateCompositeProductUseCase createCompositeProductUseCase;
    private final ListProductsSummaryUseCase listProductsSummaryUseCase;
    private final GetProductDetailsUseCase getProductDetailsUseCase;
    private final UpdateVariantPricingUseCase updateVariantPricingUseCase;

    @PostMapping
    public ResponseEntity<String> createProduct(@Valid @RequestBody CompositeProductRequestDTO request) {
        return ResponseEntity.ok(createCompositeProductUseCase.execute(request));
    }

    @GetMapping
    public ResponseEntity<Page<ProductSummaryDTO>> getProducts(
            @RequestParam(required = false) String typeId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(listProductsSummaryUseCase.execute(typeId, minPrice, maxPrice, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        return getProductDetailsUseCase.execute(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{productId}/variants/{variantId}")
    public ResponseEntity<Void> patchVariant(@PathVariable String productId,
                                             @PathVariable String variantId,
                                             @Valid @RequestBody UpdateVariantPricingUseCase.PatchVariantRequest request) {
        updateVariantPricingUseCase.execute(productId, variantId, request);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
