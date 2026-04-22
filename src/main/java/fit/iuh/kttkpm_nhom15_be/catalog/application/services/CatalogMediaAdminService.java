package fit.iuh.kttkpm_nhom15_be.catalog.application.services;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogMediaAdminDtos.MediaResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.events.CatalogProductChangedEvent;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Media;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.MediaType;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.CatalogAdminRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.MediaRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiNotFoundException;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.FileStoragePort;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.StoredFile;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.UploadFileCommand;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.audit.AdminAuditService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogMediaAdminService {

  private final CatalogAdminRepository catalogAdminRepository;
  private final MediaRepository mediaRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final AdminAuditService adminAuditService;
  private final FileStoragePort fileStoragePort;

  public List<MediaResponse> getProductMedia(String productId) {
    ensureProductExists(productId);
    return mediaRepository.findProductMedia(productId).stream().map(this::toResponse).toList();
  }

  public MediaResponse getProductMedia(String productId, String mediaId) {
    ensureProductExists(productId);
    return toResponse(requireProductMedia(productId, mediaId));
  }

  @Transactional
  public MediaResponse createProductMedia(String productId, MultipartFile file, MediaType type, boolean primary) {
    ensureProductExists(productId);
    Media media = saveNewMedia(file, productId, null, type, primary);
    publishCatalogChanged(productId, "ADMIN_PRODUCT_MEDIA_CREATED");
    adminAuditService.log("PRODUCT_MEDIA_CREATED", "MEDIA", media.getId(), "{\"productId\":\"" + productId + "\"}");
    return toResponse(media);
  }

  @Transactional
  public MediaResponse updateProductMedia(String productId, String mediaId, MultipartFile file, MediaType type, boolean primary) {
    Media existing = requireProductMedia(productId, mediaId);
    Media media = saveExistingMedia(existing, file, type, primary);
    publishCatalogChanged(productId, "ADMIN_PRODUCT_MEDIA_UPDATED");
    adminAuditService.log("PRODUCT_MEDIA_UPDATED", "MEDIA", mediaId, "{\"productId\":\"" + productId + "\"}");
    return toResponse(media);
  }

  @Transactional
  public void deleteProductMedia(String productId, String mediaId) {
    Media media = requireProductMedia(productId, mediaId);
    mediaRepository.deleteById(mediaId);
    removeStoredObject(media.getPublicId());
    publishCatalogChanged(productId, "ADMIN_PRODUCT_MEDIA_DELETED");
    adminAuditService.log("PRODUCT_MEDIA_DELETED", "MEDIA", mediaId, "{\"productId\":\"" + productId + "\"}");
  }

  @Transactional
  public MediaResponse setPrimaryProductMedia(String productId, String mediaId) {
    Media existing = requireProductMedia(productId, mediaId);
    mediaRepository.clearPrimaryForProduct(productId);
    existing.setPrimary(true);
    Media saved = mediaRepository.save(existing);
    publishCatalogChanged(productId, "ADMIN_PRODUCT_MEDIA_PRIMARY_SET");
    adminAuditService.log("PRODUCT_MEDIA_PRIMARY_SET", "MEDIA", mediaId, "{\"productId\":\"" + productId + "\"}");
    return toResponse(saved);
  }

  public List<MediaResponse> getVariantMedia(String variantId) {
    ensureVariantExists(variantId);
    return mediaRepository.findVariantMedia(variantId).stream().map(this::toResponse).toList();
  }

  public MediaResponse getVariantMedia(String variantId, String mediaId) {
    ensureVariantExists(variantId);
    return toResponse(requireVariantMedia(variantId, mediaId));
  }

  @Transactional
  public MediaResponse createVariantMedia(String variantId, MultipartFile file, MediaType type, boolean primary) {
    String productId = ensureVariantExists(variantId);
    Media media = saveNewMedia(file, productId, variantId, type, primary);
    publishCatalogChanged(productId, "ADMIN_VARIANT_MEDIA_CREATED");
    adminAuditService.log("VARIANT_MEDIA_CREATED", "MEDIA", media.getId(), "{\"variantId\":\"" + variantId + "\"}");
    return toResponse(media);
  }

  @Transactional
  public MediaResponse updateVariantMedia(String variantId, String mediaId, MultipartFile file, MediaType type, boolean primary) {
    String productId = ensureVariantExists(variantId);
    Media existing = requireVariantMedia(variantId, mediaId);
    Media media = saveExistingMedia(existing, file, type, primary);
    publishCatalogChanged(productId, "ADMIN_VARIANT_MEDIA_UPDATED");
    adminAuditService.log("VARIANT_MEDIA_UPDATED", "MEDIA", mediaId, "{\"variantId\":\"" + variantId + "\"}");
    return toResponse(media);
  }

  @Transactional
  public void deleteVariantMedia(String variantId, String mediaId) {
    String productId = ensureVariantExists(variantId);
    Media media = requireVariantMedia(variantId, mediaId);
    mediaRepository.deleteById(mediaId);
    removeStoredObject(media.getPublicId());
    publishCatalogChanged(productId, "ADMIN_VARIANT_MEDIA_DELETED");
    adminAuditService.log("VARIANT_MEDIA_DELETED", "MEDIA", mediaId, "{\"variantId\":\"" + variantId + "\"}");
  }

  @Transactional
  public MediaResponse setPrimaryVariantMedia(String variantId, String mediaId) {
    String productId = ensureVariantExists(variantId);
    Media existing = requireVariantMedia(variantId, mediaId);
    mediaRepository.clearPrimaryForVariant(variantId);
    existing.setPrimary(true);
    Media saved = mediaRepository.save(existing);
    publishCatalogChanged(productId, "ADMIN_VARIANT_MEDIA_PRIMARY_SET");
    adminAuditService.log("VARIANT_MEDIA_PRIMARY_SET", "MEDIA", mediaId, "{\"variantId\":\"" + variantId + "\"}");
    return toResponse(saved);
  }

  private Media saveNewMedia(MultipartFile file, String productId, String variantId, MediaType type, boolean primary) {
    validateFile(file);
    if (primary) {
      clearScopePrimary(productId, variantId);
    }
    StoredFile storedFile = uploadFile(file, buildScope(productId, variantId));
    return mediaRepository.save(Media.builder()
      .productId(productId)
      .variantId(variantId)
      .url(storedFile.url())
      .publicId(storedFile.objectKey())
      .type(type == null ? MediaType.IMAGE : type)
      .isPrimary(primary)
      .build());
  }

  private Media saveExistingMedia(Media existing, MultipartFile file, MediaType type, boolean primary) {
    validateFile(file);
    if (primary) {
      clearScopePrimary(existing.getProductId(), existing.getVariantId());
    }
    String previousObjectKey = existing.getPublicId();
    StoredFile storedFile = uploadFile(file, buildScope(existing.getProductId(), existing.getVariantId()));
    existing.setUrl(storedFile.url());
    existing.setPublicId(storedFile.objectKey());
    existing.setType(type == null ? existing.getType() : type);
    existing.setPrimary(primary);
    Media saved = mediaRepository.save(existing);
    removeStoredObject(previousObjectKey);
    return saved;
  }

  private void clearScopePrimary(String productId, String variantId) {
    if (variantId == null || variantId.isBlank()) {
      mediaRepository.clearPrimaryForProduct(productId);
      return;
    }
    mediaRepository.clearPrimaryForVariant(variantId);
  }

  private Media requireProductMedia(String productId, String mediaId) {
    Media media = mediaRepository.findById(mediaId)
      .orElseThrow(() -> new ApiNotFoundException("Media not found"));
    if (!productId.equals(media.getProductId()) || media.getVariantId() != null) {
      throw new ApiNotFoundException("Product media not found");
    }
    return media;
  }

  private Media requireVariantMedia(String variantId, String mediaId) {
    Media media = mediaRepository.findById(mediaId)
      .orElseThrow(() -> new ApiNotFoundException("Media not found"));
    if (!variantId.equals(media.getVariantId())) {
      throw new ApiNotFoundException("Variant media not found");
    }
    return media;
  }

  private void ensureProductExists(String productId) {
    if (!catalogAdminRepository.existsProduct(productId)) {
      throw new ApiNotFoundException("Product not found");
    }
  }

  private String ensureVariantExists(String variantId) {
    if (!catalogAdminRepository.existsVariant(variantId)) {
      throw new ApiNotFoundException("Variant not found");
    }
    return catalogAdminRepository.findProductIdByVariantId(variantId);
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Cannot upload empty file.");
    }
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

  private void removeStoredObject(String objectKey) {
    try {
      fileStoragePort.delete(objectKey);
    } catch (Exception ex) {
      log.warn("Cannot remove stored object '{}': {}", objectKey, ex.getMessage());
    }
  }

  private void publishCatalogChanged(String productId, String reason) {
    eventPublisher.publishEvent(new CatalogProductChangedEvent(productId, reason, LocalDateTime.now()));
  }

  private MediaResponse toResponse(Media media) {
    return new MediaResponse(
      media.getId(),
      media.getProductId(),
      media.getVariantId(),
      media.getUrl(),
      media.getPublicId(),
      media.getType(),
      media.isPrimary()
    );
  }
}
