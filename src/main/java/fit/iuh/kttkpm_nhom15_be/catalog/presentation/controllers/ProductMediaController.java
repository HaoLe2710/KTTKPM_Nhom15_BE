package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.media.UploadMediaUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Media;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductMediaController {

    private final UploadMediaUseCase uploadMediaUseCase;

    @Operation(deprecated = true)
    @PostMapping("/{productId}/media")
    public ResponseEntity<Media> uploadProductMedia(@PathVariable String productId,
                                                    @RequestParam("file") MultipartFile file) {
        Media media = uploadMediaUseCase.execute(file, productId, null);
        return ResponseEntity.ok(media);
    }

    @Operation(deprecated = true)
    @PostMapping("/{productId}/variants/{variantId}/media")
    public ResponseEntity<Media> uploadVariantMedia(@PathVariable String productId,
                                                    @PathVariable String variantId,
                                                    @RequestParam("file") MultipartFile file) {
        Media media = uploadMediaUseCase.execute(file, productId, variantId);
        return ResponseEntity.ok(media);
    }
}
