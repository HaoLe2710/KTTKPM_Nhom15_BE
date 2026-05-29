package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.PublicProductDetailResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.CreatedResourceResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductCreateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductVariantCreateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.VariantOptionAssignmentRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.VariantUpdateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CompositeProductRequestDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.ProductSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.services.CatalogAdminService;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product.GetPublicProductDetailUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product.ListProductsSummaryUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product.UpdateVariantPricingUseCase;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiNotFoundException;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

  private final ListProductsSummaryUseCase listProductsSummaryUseCase;
  private final GetPublicProductDetailUseCase getPublicProductDetailUseCase;
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
  public ResponseEntity<Page<ProductSummaryDTO>> getProducts(@RequestParam(required = false) String typeId,
                                                             @RequestParam(required = false) BigDecimal minPrice,
                                                             @RequestParam(required = false) BigDecimal maxPrice,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(listProductsSummaryUseCase.execute(typeId, minPrice, maxPrice, page, size));
  }

  @Operation(deprecated = true)
  @GetMapping("/{id}")
  public ResponseEntity<PublicProductDetailResponse> getProduct(@PathVariable String id) {
    return ResponseEntity.ok(getPublicProductDetailUseCase.execute(id));
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
