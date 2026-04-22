package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.media;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Media;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.MediaType;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.MediaRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.FileStoragePort;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.StoredFile;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.UploadFileCommand;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UploadMediaUseCase {

    private final MediaRepository mediaRepository;
    private final FileStoragePort fileStoragePort;

    public Media execute(MultipartFile file, String productId, String variantId) {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file.");
        }

        StoredFile storedFile = uploadFile(file, buildScope(productId, variantId));

        Media newMedia = Media.builder()
                .productId(productId)
                .variantId(variantId)
                .url(storedFile.url())
                .publicId(storedFile.objectKey())
                .type(MediaType.IMAGE) // Assuming image for this prototype
                .isPrimary(false)
                .build();

        return mediaRepository.save(newMedia);
    }

    private StoredFile uploadFile(MultipartFile file, String scope) {
        try {
            return fileStoragePort.upload(new UploadFileCommand(
                    scope,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            ));
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read uploaded file bytes", ex);
        }
    }

    private String buildScope(String productId, String variantId) {
        if (variantId == null || variantId.isBlank()) {
            return "catalog/products/" + productId;
        }
        return "catalog/products/" + productId + "/variants/" + variantId;
    }
}
