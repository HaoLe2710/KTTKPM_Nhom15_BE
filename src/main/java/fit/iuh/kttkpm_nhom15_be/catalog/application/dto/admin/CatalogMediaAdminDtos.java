package fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.MediaType;

public final class CatalogMediaAdminDtos {

  private CatalogMediaAdminDtos() {
  }

  public record MediaResponse(
    String id,
    String productId,
    String variantId,
    String url,
    String publicId,
    MediaType type,
    boolean primary
  ) {
  }
}
