package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers.admin;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.ProductTypeRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.ProductTypeResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.CreateProductTypeUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.DeleteProductTypeUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.GetProductTypesUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.UpdateProductTypeUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Catalog")
@RestController
@RequestMapping("/api/v1/admin/product-types")
@RequiredArgsConstructor
public class AdminProductTypeController {

  private final GetProductTypesUseCase getProductTypesUseCase;
  private final CreateProductTypeUseCase createProductTypeUseCase;
  private final UpdateProductTypeUseCase updateProductTypeUseCase;
  private final DeleteProductTypeUseCase deleteProductTypeUseCase;

  @GetMapping
  public ResponseEntity<List<ProductTypeResponse>> getProductTypes() {
    return ResponseEntity.ok(getProductTypesUseCase.execute());
  }

  @PostMapping
  public ResponseEntity<ProductTypeResponse> createProductType(@Valid @RequestBody ProductTypeRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(createProductTypeUseCase.execute(request));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductTypeResponse> updateProductType(@PathVariable String id,
                                                               @Valid @RequestBody ProductTypeRequest request) {
    return ResponseEntity.ok(updateProductTypeUseCase.execute(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProductType(@PathVariable String id) {
    deleteProductTypeUseCase.execute(id);
    return ResponseEntity.noContent().build();
  }
}
