package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.media;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Media;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.MediaType;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.MediaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UploadMediaUseCaseTest {

    @Test
    void executeRejectsEmptyFiles() {
        MediaRepository mediaRepository = Mockito.mock(MediaRepository.class);
        MultipartFile file = Mockito.mock(MultipartFile.class);
        UploadMediaUseCase useCase = new UploadMediaUseCase(mediaRepository);

        when(file.isEmpty()).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(file, "product-1", "variant-1"));
        verify(mediaRepository, never()).save(any(Media.class));
    }

    @Test
    void executeBuildsAndPersistsMediaMetadata() {
        MediaRepository mediaRepository = Mockito.mock(MediaRepository.class);
        MultipartFile file = Mockito.mock(MultipartFile.class);
        UploadMediaUseCase useCase = new UploadMediaUseCase(mediaRepository);

        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("shoe.png");
        when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> {
            Media media = invocation.getArgument(0);
            media.setId("media-1");
            return media;
        });

        Media media = useCase.execute(file, "product-1", "variant-1");

        ArgumentCaptor<Media> savedMedia = ArgumentCaptor.forClass(Media.class);
        verify(mediaRepository).save(savedMedia.capture());
        assertEquals("media-1", media.getId());
        assertEquals("product-1", savedMedia.getValue().getProductId());
        assertEquals("variant-1", savedMedia.getValue().getVariantId());
        assertEquals(MediaType.IMAGE, savedMedia.getValue().getType());
        assertFalse(savedMedia.getValue().isPrimary());
        assertTrue(savedMedia.getValue().getUrl().contains("shoe.png"));
        assertTrue(savedMedia.getValue().getPublicId().startsWith("mock-"));
        assertNotNull(savedMedia.getValue().getUrl());
    }
}
