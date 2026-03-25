package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.media;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Media;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.MediaType;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadMediaUseCase {

    private final MediaRepository mediaRepository;

    public Media execute(MultipartFile file, String productId, String variantId) {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file.");
        }

        // Simulate external storage (e.g. AWS S3 / Cloudinary)
        String mockUrl = "https://mock-image-server.com/uploads/" + UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
        String mockPublicId = "mock-" + System.currentTimeMillis();

        Media newMedia = Media.builder()
                .productId(productId)
                .variantId(variantId)
                .url(mockUrl)
                .publicId(mockPublicId)
                .type(MediaType.IMAGE) // Assuming image for this prototype
                .isPrimary(false)
                .build();

        return mediaRepository.save(newMedia);
    }
}
