package fit.iuh.kttkpm_nhom15_be.search.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.FailureResolveRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionFailureResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SynonymGroupWriteRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.results.TriggerFullSearchProjectionRebuildResult;
import fit.iuh.kttkpm_nhom15_be.search.application.usecases.projection.TriggerFullSearchProjectionRebuildUseCase;
import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchAdminRepository;
import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchProjectionRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.audit.AdminAuditService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SearchAdminServiceTest {

  @Test
  void retryFailureOnlyAllowsOpenFailuresAndRetainsFailureRecord() {
    SearchAdminRepository adminRepository = Mockito.mock(SearchAdminRepository.class);
    SearchProjectionRepository projectionRepository = Mockito.mock(SearchProjectionRepository.class);
    TriggerFullSearchProjectionRebuildUseCase rebuildUseCase = Mockito.mock(TriggerFullSearchProjectionRebuildUseCase.class);
    AdminAuditService auditService = Mockito.mock(AdminAuditService.class);
    SearchAdminService service = new SearchAdminService(adminRepository, projectionRepository, rebuildUseCase, auditService);

    when(adminRepository.findProjectionFailure("product-1")).thenReturn(Optional.of(new ProjectionFailureResponse(
      "product-1",
      "CATALOG_PRODUCT_CHANGED",
      "boom",
      6,
      LocalDateTime.now(),
      null,
      "OPEN",
      null,
      null,
      null,
      LocalDateTime.now()
    )));

    var response = service.retryFailure("product-1");

    assertEquals("RETRY_QUEUED", response.status());
    verify(projectionRepository).enqueueProjectionTask("product-1", "ADMIN_RETRY");
    verify(adminRepository).markFailureRetryQueued("product-1");
  }

  @Test
  void retryFailureRejectsResolvedFailures() {
    SearchAdminRepository adminRepository = Mockito.mock(SearchAdminRepository.class);
    SearchProjectionRepository projectionRepository = Mockito.mock(SearchProjectionRepository.class);
    TriggerFullSearchProjectionRebuildUseCase rebuildUseCase = Mockito.mock(TriggerFullSearchProjectionRebuildUseCase.class);
    AdminAuditService auditService = Mockito.mock(AdminAuditService.class);
    SearchAdminService service = new SearchAdminService(adminRepository, projectionRepository, rebuildUseCase, auditService);

    when(adminRepository.findProjectionFailure("product-1")).thenReturn(Optional.of(new ProjectionFailureResponse(
      "product-1",
      "CATALOG_PRODUCT_CHANGED",
      "boom",
      6,
      LocalDateTime.now(),
      null,
      "RESOLVED_AUTOMATIC",
      "AUTOMATIC",
      null,
      LocalDateTime.now(),
      LocalDateTime.now()
    )));

    assertThrows(ApiValidationException.class, () -> service.retryFailure("product-1"));
    verify(projectionRepository, never()).enqueueProjectionTask(anyString(), anyString());
  }

  @Test
  void resolveFailureAllowsRetryQueuedFailures() {
    SearchAdminRepository adminRepository = Mockito.mock(SearchAdminRepository.class);
    SearchProjectionRepository projectionRepository = Mockito.mock(SearchProjectionRepository.class);
    TriggerFullSearchProjectionRebuildUseCase rebuildUseCase = Mockito.mock(TriggerFullSearchProjectionRebuildUseCase.class);
    AdminAuditService auditService = Mockito.mock(AdminAuditService.class);
    SearchAdminService service = new SearchAdminService(adminRepository, projectionRepository, rebuildUseCase, auditService);

    when(adminRepository.findProjectionFailure("product-1")).thenReturn(Optional.of(new ProjectionFailureResponse(
      "product-1",
      "CATALOG_PRODUCT_CHANGED",
      "boom",
      6,
      LocalDateTime.now(),
      LocalDateTime.now(),
      "RETRY_QUEUED",
      null,
      null,
      null,
      LocalDateTime.now()
    )));

    service.resolveFailure("product-1", new FailureResolveRequest("skip"));

    verify(adminRepository).clearProjectionTask("product-1");
    verify(adminRepository).markFailureResolvedManual("product-1", "skip");
  }

  @Test
  void createSynonymGroupRejectsActiveGroupsWithoutTerms() {
    SearchAdminRepository adminRepository = Mockito.mock(SearchAdminRepository.class);
    SearchProjectionRepository projectionRepository = Mockito.mock(SearchProjectionRepository.class);
    TriggerFullSearchProjectionRebuildUseCase rebuildUseCase = Mockito.mock(TriggerFullSearchProjectionRebuildUseCase.class);
    AdminAuditService auditService = Mockito.mock(AdminAuditService.class);
    SearchAdminService service = new SearchAdminService(adminRepository, projectionRepository, rebuildUseCase, auditService);

    when(adminRepository.existsSynonymGroupCode("vi", "cleanser", null)).thenReturn(false);

    assertThrows(ApiValidationException.class,
      () -> service.createSynonymGroup(new SynonymGroupWriteRequest("cleanser", "vi", true)));
  }
}
