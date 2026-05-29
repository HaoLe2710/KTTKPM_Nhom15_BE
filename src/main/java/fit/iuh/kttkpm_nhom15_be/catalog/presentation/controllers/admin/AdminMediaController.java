package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers.admin;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogMediaAdminDtos.MediaResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.services.CatalogMediaAdminService;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.MediaType;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.MessageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Admin Catalog")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminMediaController {

  private final CatalogMediaAdminService catalogMediaAdminService;

  @GetMapping("/products/{productId}/media")
  public ResponseEntity<List<MediaResponse>> getProductMedia(@PathVariable String productId) {
    return ResponseEntity.ok(catalogMediaAdminService.getProductMedia(productId));
  }

  @GetMapping("/products/{productId}/media/{mediaId}")
  public ResponseEntity<MediaResponse> getProductMedia(@PathVariable String productId,
                                                       @PathVariable String mediaId) {
    return ResponseEntity.ok(catalogMediaAdminService.getProductMedia(productId, mediaId));
  }

  @PostMapping(path = "/products/{productId}/media", consumes = "multipart/form-data")
  public ResponseEntity<MediaResponse> createProductMedia(@PathVariable String productId,
                                                          @RequestParam("file") MultipartFile file,
                                                          @RequestParam(defaultValue = "IMAGE") MediaType type,
                                                          @RequestParam(defaultValue = "false") boolean primary) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogMediaAdminService.createProductMedia(productId, file, type, primary));
  }

  @PutMapping(path = "/products/{productId}/media/{mediaId}", consumes = "multipart/form-data")
  public ResponseEntity<MediaResponse> updateProductMedia(@PathVariable String productId,
                                                          @PathVariable String mediaId,
                                                          @RequestParam("file") MultipartFile file,
                                                          @RequestParam(defaultValue = "IMAGE") MediaType type,
                                                          @RequestParam(defaultValue = "false") boolean primary) {
    return ResponseEntity.ok(catalogMediaAdminService.updateProductMedia(productId, mediaId, file, type, primary));
  }

  @DeleteMapping("/products/{productId}/media/{mediaId}")
  public ResponseEntity<MessageResponse> deleteProductMedia(@PathVariable String productId,
                                                            @PathVariable String mediaId) {
    catalogMediaAdminService.deleteProductMedia(productId, mediaId);
    return ResponseEntity.ok(new MessageResponse("Media của sản phẩm đã được xóa thành công"));
  }

  @PatchMapping("/products/{productId}/media/{mediaId}/primary")
  public ResponseEntity<MediaResponse> setPrimaryProductMedia(@PathVariable String productId,
                                                              @PathVariable String mediaId) {
    return ResponseEntity.ok(catalogMediaAdminService.setPrimaryProductMedia(productId, mediaId));
  }

  @GetMapping("/variants/{variantId}/media")
  public ResponseEntity<List<MediaResponse>> getVariantMedia(@PathVariable String variantId) {
    return ResponseEntity.ok(catalogMediaAdminService.getVariantMedia(variantId));
  }

  @GetMapping("/variants/{variantId}/media/{mediaId}")
  public ResponseEntity<MediaResponse> getVariantMedia(@PathVariable String variantId,
                                                       @PathVariable String mediaId) {
    return ResponseEntity.ok(catalogMediaAdminService.getVariantMedia(variantId, mediaId));
  }

  @PostMapping(path = "/variants/{variantId}/media", consumes = "multipart/form-data")
  public ResponseEntity<MediaResponse> createVariantMedia(@PathVariable String variantId,
                                                          @RequestParam("file") MultipartFile file,
                                                          @RequestParam(defaultValue = "IMAGE") MediaType type,
                                                          @RequestParam(defaultValue = "false") boolean primary) {
    return ResponseEntity.status(HttpStatus.CREATED).body(catalogMediaAdminService.createVariantMedia(variantId, file, type, primary));
  }

  @PutMapping(path = "/variants/{variantId}/media/{mediaId}", consumes = "multipart/form-data")
  public ResponseEntity<MediaResponse> updateVariantMedia(@PathVariable String variantId,
                                                          @PathVariable String mediaId,
                                                          @RequestParam("file") MultipartFile file,
                                                          @RequestParam(defaultValue = "IMAGE") MediaType type,
                                                          @RequestParam(defaultValue = "false") boolean primary) {
    return ResponseEntity.ok(catalogMediaAdminService.updateVariantMedia(variantId, mediaId, file, type, primary));
  }

  @DeleteMapping("/variants/{variantId}/media/{mediaId}")
  public ResponseEntity<MessageResponse> deleteVariantMedia(@PathVariable String variantId,
                                                            @PathVariable String mediaId) {
    catalogMediaAdminService.deleteVariantMedia(variantId, mediaId);
    return ResponseEntity.ok(new MessageResponse("Media của biến thể đã được xóa thành công"));
  }

  @PatchMapping("/variants/{variantId}/media/{mediaId}/primary")
  public ResponseEntity<MediaResponse> setPrimaryVariantMedia(@PathVariable String variantId,
                                                              @PathVariable String mediaId) {
    return ResponseEntity.ok(catalogMediaAdminService.setPrimaryVariantMedia(variantId, mediaId));
  }
}
