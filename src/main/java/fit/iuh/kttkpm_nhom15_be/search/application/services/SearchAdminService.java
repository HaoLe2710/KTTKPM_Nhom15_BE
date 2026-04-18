package fit.iuh.kttkpm_nhom15_be.search.application.services;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.FailureResolveRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionEnqueueResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionFailureResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionHealthSummaryResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionRunResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionTaskResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SearchPreviewResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SuggestionResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SuggestionWriteRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SynonymGroupResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SynonymGroupWriteRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SynonymTermWriteRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.TopClickedProductResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.TopQueryResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ZeroResultQueryResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.results.TriggerFullSearchProjectionRebuildResult;
import fit.iuh.kttkpm_nhom15_be.search.application.support.SearchNormalizer;
import fit.iuh.kttkpm_nhom15_be.search.application.usecases.projection.TriggerFullSearchProjectionRebuildUseCase;
import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchAdminRepository;
import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchProjectionRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiConflictException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiNotFoundException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.audit.AdminAuditService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchAdminService {

  private final SearchAdminRepository searchAdminRepository;
  private final SearchProjectionRepository searchProjectionRepository;
  private final TriggerFullSearchProjectionRebuildUseCase triggerFullSearchProjectionRebuildUseCase;
  private final AdminAuditService adminAuditService;

  @Transactional
  public ProjectionEnqueueResponse rebuildOne(String productId) {
    ensureProductExists(productId);
    searchProjectionRepository.enqueueProjectionTask(productId, "ADMIN_REBUILD");
    adminAuditService.log("PROJECTION_REBUILD_ENQUEUED", "PRODUCT", productId, "{\"reason\":\"ADMIN_REBUILD\"}");
    return new ProjectionEnqueueResponse(productId, "QUEUED");
  }

  public TriggerFullSearchProjectionRebuildResult rebuildAll() {
    TriggerFullSearchProjectionRebuildResult result = triggerFullSearchProjectionRebuildUseCase.execute();
    adminAuditService.log("PROJECTION_REBUILD_ALL_ENQUEUED", "SEARCH_PROJECTION_RUN", result.runId(), "{\"queuedProducts\":" + result.queuedProducts() + "}");
    return result;
  }

  public Page<ProjectionTaskResponse> getProjectionTasks(AdminPageRequest pageRequest) {
    return searchAdminRepository.findProjectionTasks(pageRequest);
  }

  public Page<ProjectionFailureResponse> getProjectionFailures(String state, AdminPageRequest pageRequest) {
    return searchAdminRepository.findProjectionFailures(state, pageRequest);
  }

  public Page<ProjectionRunResponse> getProjectionRuns(AdminPageRequest pageRequest) {
    return searchAdminRepository.findProjectionRuns(pageRequest);
  }

  @Transactional
  public ProjectionEnqueueResponse retryFailure(String productId) {
    ProjectionFailureResponse failure = searchAdminRepository.findProjectionFailure(productId)
      .orElseThrow(() -> new ApiNotFoundException("Projection failure not found"));
    if (!"OPEN".equals(failure.state())) {
      throw new ApiValidationException("Only OPEN failures can be retried");
    }
    searchProjectionRepository.enqueueProjectionTask(productId, "ADMIN_RETRY");
    searchAdminRepository.markFailureRetryQueued(productId);
    adminAuditService.log("PROJECTION_FAILURE_RETRIED", "PRODUCT", productId, "{\"state\":\"RETRY_QUEUED\"}");
    return new ProjectionEnqueueResponse(productId, "RETRY_QUEUED");
  }

  @Transactional
  public void resolveFailure(String productId, FailureResolveRequest request) {
    ProjectionFailureResponse failure = searchAdminRepository.findProjectionFailure(productId)
      .orElseThrow(() -> new ApiNotFoundException("Projection failure not found"));
    if (!"OPEN".equals(failure.state()) && !"RETRY_QUEUED".equals(failure.state())) {
      throw new ApiValidationException("Only OPEN or RETRY_QUEUED failures can be resolved manually");
    }
    searchAdminRepository.clearProjectionTask(productId);
    searchAdminRepository.markFailureResolvedManual(productId, request == null ? null : request.resolutionNote());
    adminAuditService.log("PROJECTION_FAILURE_RESOLVED", "PRODUCT", productId, "{\"resolutionType\":\"MANUAL\"}");
  }

  public Page<SuggestionResponse> getSuggestions(String locale, Boolean active, AdminPageRequest pageRequest) {
    return searchAdminRepository.findSuggestions(locale, active, pageRequest);
  }

  @Transactional
  public ProjectionEnqueueResponse createSuggestion(SuggestionWriteRequest request) {
    validateSuggestionUniqueness(request.locale(), request.keyword(), null);
    String id = searchAdminRepository.insertSuggestion(
      request.keyword().trim(),
      SearchNormalizer.normalizeText(request.keyword()),
      request.locale().trim(),
      request.weight(),
      request.isActive()
    );
    adminAuditService.log("SUGGESTION_CREATED", "SEARCH_SUGGESTION", id, "{\"locale\":\"" + request.locale().trim() + "\"}");
    return new ProjectionEnqueueResponse(id, "CREATED");
  }

  @Transactional
  public void updateSuggestion(String id, SuggestionWriteRequest request) {
    ensureSuggestionExists(id);
    validateSuggestionUniqueness(request.locale(), request.keyword(), id);
    searchAdminRepository.updateSuggestion(
      id,
      request.keyword().trim(),
      SearchNormalizer.normalizeText(request.keyword()),
      request.locale().trim(),
      request.weight(),
      request.isActive()
    );
    adminAuditService.log("SUGGESTION_UPDATED", "SEARCH_SUGGESTION", id, "{\"locale\":\"" + request.locale().trim() + "\"}");
  }

  @Transactional
  public void toggleSuggestionActive(String id, boolean active) {
    ensureSuggestionExists(id);
    searchAdminRepository.updateSuggestionActive(id, active);
    adminAuditService.log("SUGGESTION_ACTIVE_TOGGLED", "SEARCH_SUGGESTION", id, "{\"active\":" + active + "}");
  }

  public Page<SynonymGroupResponse> getSynonymGroups(String locale, Boolean active, AdminPageRequest pageRequest) {
    return searchAdminRepository.findSynonymGroups(locale, active, pageRequest);
  }

  @Transactional
  public ProjectionEnqueueResponse createSynonymGroup(SynonymGroupWriteRequest request) {
    validateSynonymGroupCode(request.locale(), request.code(), null);
    if (request.isActive()) {
      throw new ApiValidationException("A synonym group cannot be activated before it has at least 2 terms");
    }
    String id = searchAdminRepository.insertSynonymGroup(request.code().trim(), request.locale().trim(), false);
    adminAuditService.log("SYNONYM_GROUP_CREATED", "SEARCH_SYNONYM_GROUP", id, "{\"locale\":\"" + request.locale().trim() + "\"}");
    return new ProjectionEnqueueResponse(id, "CREATED");
  }

  @Transactional
  public void updateSynonymGroup(String id, SynonymGroupWriteRequest request) {
    ensureSynonymGroupExists(id);
    validateSynonymGroupCode(request.locale(), request.code(), id);
    if (request.isActive() && searchAdminRepository.countSynonymTerms(id) < 2) {
      throw new ApiValidationException("An active synonym group must contain at least 2 terms");
    }
    searchAdminRepository.updateSynonymGroup(id, request.code().trim(), request.locale().trim(), request.isActive());
    adminAuditService.log("SYNONYM_GROUP_UPDATED", "SEARCH_SYNONYM_GROUP", id, "{\"locale\":\"" + request.locale().trim() + "\"}");
  }

  @Transactional
  public ProjectionEnqueueResponse addSynonymTerm(String groupId, SynonymTermWriteRequest request) {
    ensureSynonymGroupExists(groupId);
    String normalizedTerm = SearchNormalizer.normalizeText(request.term());
    if (normalizedTerm == null || normalizedTerm.isBlank()) {
      throw new ApiValidationException("Synonym term is invalid after normalization");
    }
    if (searchAdminRepository.existsSynonymTerm(groupId, normalizedTerm, null)) {
      throw new ApiConflictException("Synonym term already exists in this group");
    }
    String id = searchAdminRepository.insertSynonymTerm(groupId, request.term().trim(), normalizedTerm);
    adminAuditService.log("SYNONYM_TERM_CREATED", "SEARCH_SYNONYM_TERM", id, "{\"groupId\":\"" + groupId + "\"}");
    return new ProjectionEnqueueResponse(id, "CREATED");
  }

  @Transactional
  public void deleteSynonymTerm(String termId) {
    if (!searchAdminRepository.existsSynonymTermId(termId)) {
      throw new ApiNotFoundException("Synonym term not found");
    }
    searchAdminRepository.deleteSynonymTerm(termId);
    adminAuditService.log("SYNONYM_TERM_DELETED", "SEARCH_SYNONYM_TERM", termId, "{}");
  }

  public List<TopQueryResponse> getTopQueries(LocalDateTime from, LocalDateTime to, String locale, int limit) {
    return searchAdminRepository.findTopQueries(resolveFrom(from), resolveTo(to), locale, resolveLimit(limit));
  }

  public Page<ZeroResultQueryResponse> getZeroResultQueries(LocalDateTime from, LocalDateTime to, String locale, AdminPageRequest pageRequest) {
    return searchAdminRepository.findZeroResultQueries(resolveFrom(from), resolveTo(to), locale, pageRequest);
  }

  public List<TopClickedProductResponse> getTopClickedProducts(LocalDateTime from, LocalDateTime to, String locale, int limit) {
    return searchAdminRepository.findTopClickedProducts(resolveFrom(from), resolveTo(to), locale, resolveLimit(limit));
  }

  public ProjectionHealthSummaryResponse getProjectionHealthSummary() {
    return searchAdminRepository.getProjectionHealthSummary();
  }

  public SearchPreviewResponse getSearchPreview(String productId) {
    ensureProductExists(productId);
    return searchAdminRepository.findSearchPreview(productId)
      .orElseThrow(() -> new ApiNotFoundException("Search preview not found"));
  }

  private void validateSuggestionUniqueness(String locale, String keyword, String excludeId) {
    String normalizedKeyword = SearchNormalizer.normalizeText(keyword);
    if (normalizedKeyword == null || normalizedKeyword.isBlank()) {
      throw new ApiValidationException("Suggestion keyword is invalid after normalization");
    }
    if (searchAdminRepository.existsSuggestion(locale.trim(), normalizedKeyword, excludeId)) {
      throw new ApiConflictException("Suggestion already exists for this locale");
    }
  }

  private void validateSynonymGroupCode(String locale, String code, String excludeId) {
    if (searchAdminRepository.existsSynonymGroupCode(locale.trim(), code.trim(), excludeId)) {
      throw new ApiConflictException("Synonym group code already exists for this locale");
    }
  }

  private void ensureProductExists(String productId) {
    if (!searchAdminRepository.existsProduct(productId)) {
      throw new ApiNotFoundException("Product not found");
    }
  }

  private void ensureSuggestionExists(String id) {
    if (!searchAdminRepository.existsSuggestionId(id)) {
      throw new ApiNotFoundException("Suggestion not found");
    }
  }

  private void ensureSynonymGroupExists(String id) {
    if (!searchAdminRepository.existsSynonymGroup(id)) {
      throw new ApiNotFoundException("Synonym group not found");
    }
  }

  private LocalDateTime resolveFrom(LocalDateTime from) {
    return from == null ? LocalDateTime.now().minusDays(7) : from;
  }

  private LocalDateTime resolveTo(LocalDateTime to) {
    return to == null ? LocalDateTime.now() : to;
  }

  private int resolveLimit(int limit) {
    int resolved = limit <= 0 ? 20 : limit;
    if (resolved > 100) {
      throw new ApiValidationException("limit must be between 1 and 100");
    }
    return resolved;
  }
}
