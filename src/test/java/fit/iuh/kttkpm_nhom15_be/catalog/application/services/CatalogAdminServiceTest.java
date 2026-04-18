package fit.iuh.kttkpm_nhom15_be.catalog.application.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductCreateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.ProductVariantCreateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.VariantCreateRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.VariantOptionAssignmentRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.CatalogAdminRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiConflictException;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.audit.AdminAuditService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

class CatalogAdminServiceTest {

  @Test
  void createProductRejectsDuplicateSlugIgnoringCase() {
    CatalogAdminRepository repository = Mockito.mock(CatalogAdminRepository.class);
    ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
    AdminAuditService auditService = Mockito.mock(AdminAuditService.class);
    CatalogAdminService service = new CatalogAdminService(repository, publisher, auditService);

    when(repository.existsProductType("type-1")).thenReturn(true);
    when(repository.existsProductSlugIgnoreCase("serum-ban-dem", null)).thenReturn(true);

    assertThrows(ApiConflictException.class, () -> service.createProduct(new ProductCreateRequest(
      "type-1",
      "Serum Ban Dem",
      "serum-ban-dem",
      "desc",
      null,
      null,
      false,
      true,
      List.of(),
      List.of(),
      List.of(),
      List.of(),
      List.of(new ProductVariantCreateRequest(
        "SKU-001",
        new BigDecimal("100"),
        10,
        true,
        List.of()
      ))
    )));

    verify(repository, never()).insertProduct(
      org.mockito.ArgumentMatchers.anyString(),
      org.mockito.ArgumentMatchers.anyString(),
      org.mockito.ArgumentMatchers.anyString(),
      org.mockito.ArgumentMatchers.any(),
      org.mockito.ArgumentMatchers.any(),
      org.mockito.ArgumentMatchers.any(),
      org.mockito.ArgumentMatchers.anyBoolean(),
      org.mockito.ArgumentMatchers.anyBoolean()
    );
  }

  @Test
  void createVariantRejectsDuplicateNormalizedSku() {
    CatalogAdminRepository repository = Mockito.mock(CatalogAdminRepository.class);
    ApplicationEventPublisher publisher = Mockito.mock(ApplicationEventPublisher.class);
    AdminAuditService auditService = Mockito.mock(AdminAuditService.class);
    CatalogAdminService service = new CatalogAdminService(repository, publisher, auditService);

    when(repository.existsProduct("product-1")).thenReturn(true);
    when(repository.existsNormalizedSku("SKU001", null)).thenReturn(true);

    assertThrows(ApiConflictException.class, () -> service.createVariant(new VariantCreateRequest(
      "product-1",
      "sku-001",
      new BigDecimal("100"),
      3,
      true,
      List.of(new VariantOptionAssignmentRequest("option-1", "value-1"))
    )));

    verify(repository, never()).insertVariant(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyBoolean());
  }
}
