package fit.iuh.kttkpm_nhom15_be.catalog.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Media;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.MediaType;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.CatalogAdminRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.MediaRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiNotFoundException;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.audit.AdminAuditService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

class CatalogMediaAdminServiceTest {

  @Test
  void createProductMediaClearsExistingPrimaryWhenRequested() {
    CatalogAdminRepository catalogAdminRepository = Mockito.mock(CatalogAdminRepository.class);
    MediaRepository mediaRepository = Mockito.mock(MediaRepository.class);
    ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
    AdminAuditService auditService = Mockito.mock(AdminAuditService.class);
    MultipartFile file = Mockito.mock(MultipartFile.class);
    CatalogMediaAdminService service = new CatalogMediaAdminService(catalogAdminRepository, mediaRepository, publisher, auditService);

    when(catalogAdminRepository.existsProduct("product-1")).thenReturn(true);
    when(file.isEmpty()).thenReturn(false);
    when(file.getOriginalFilename()).thenReturn("image.png");
    when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> {
      Media media = invocation.getArgument(0);
      media.setId("media-1");
      return media;
    });

    var response = service.createProductMedia("product-1", file, MediaType.IMAGE, true);

    ArgumentCaptor<Media> mediaCaptor = ArgumentCaptor.forClass(Media.class);
    verify(mediaRepository).clearPrimaryForProduct("product-1");
    verify(mediaRepository).save(mediaCaptor.capture());
    assertEquals("media-1", response.id());
    assertEquals("product-1", mediaCaptor.getValue().getProductId());
    assertEquals(MediaType.IMAGE, mediaCaptor.getValue().getType());
    assertEquals(true, mediaCaptor.getValue().isPrimary());
  }

  @Test
  void setPrimaryVariantMediaClearsOtherVariantPrimaryFlags() {
    CatalogAdminRepository catalogAdminRepository = Mockito.mock(CatalogAdminRepository.class);
    MediaRepository mediaRepository = Mockito.mock(MediaRepository.class);
    ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
    AdminAuditService auditService = Mockito.mock(AdminAuditService.class);
    CatalogMediaAdminService service = new CatalogMediaAdminService(catalogAdminRepository, mediaRepository, publisher, auditService);

    when(catalogAdminRepository.existsVariant("variant-1")).thenReturn(true);
    when(catalogAdminRepository.findProductIdByVariantId("variant-1")).thenReturn("product-1");
    when(mediaRepository.findById("media-1")).thenReturn(java.util.Optional.of(Media.builder()
      .id("media-1")
      .productId("product-1")
      .variantId("variant-1")
      .url("https://example.com/image.png")
      .publicId("public-1")
      .type(MediaType.IMAGE)
      .isPrimary(false)
      .build()));
    when(mediaRepository.save(any(Media.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var response = service.setPrimaryVariantMedia("variant-1", "media-1");

    verify(mediaRepository).clearPrimaryForVariant("variant-1");
    verify(mediaRepository).save(any(Media.class));
    assertEquals(true, response.primary());
  }

  @Test
  void getProductMediaRejectsUnknownProducts() {
    CatalogAdminRepository catalogAdminRepository = Mockito.mock(CatalogAdminRepository.class);
    MediaRepository mediaRepository = Mockito.mock(MediaRepository.class);
    ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
    AdminAuditService auditService = Mockito.mock(AdminAuditService.class);
    CatalogMediaAdminService service = new CatalogMediaAdminService(catalogAdminRepository, mediaRepository, publisher, auditService);

    when(catalogAdminRepository.existsProduct("missing-product")).thenReturn(false);

    assertThrows(ApiNotFoundException.class, () -> service.getProductMedia("missing-product"));
    verify(mediaRepository, never()).findProductMedia("missing-product");
  }
}
