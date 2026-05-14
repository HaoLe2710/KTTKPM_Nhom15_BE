package fit.iuh.kttkpm_nhom15_be.search.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.IndexedFacetValueResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionFailureResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionHealthSummaryResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionRunResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ProjectionTaskResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SearchPreviewResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SuggestionResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SynonymGroupResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.TopClickedProductResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.TopQueryResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.ZeroResultQueryResponse;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface SearchAdminRepository {

  boolean existsProduct(String productId);

  Page<ProjectionTaskResponse> findProjectionTasks(AdminPageRequest pageRequest);

  Page<ProjectionFailureResponse> findProjectionFailures(String state, AdminPageRequest pageRequest);

  Page<ProjectionRunResponse> findProjectionRuns(AdminPageRequest pageRequest);

  Optional<ProjectionFailureResponse> findProjectionFailure(String productId);

  void markFailureRetryQueued(String productId);

  void markFailureResolvedManual(String productId, String resolutionNote);

  void clearProjectionTask(String productId);

  Page<SuggestionResponse> findSuggestions(String locale, Boolean active, AdminPageRequest pageRequest);

  boolean existsSuggestion(String locale, String keywordNormalized, String excludeId);

  String insertSuggestion(String keyword, String keywordNormalized, String locale, int weight, boolean active);

  void updateSuggestion(String id, String keyword, String keywordNormalized, String locale, int weight, boolean active);

  void updateSuggestionActive(String id, boolean active);

  boolean existsSuggestionId(String id);

  Page<SynonymGroupResponse> findSynonymGroups(String locale, Boolean active, AdminPageRequest pageRequest);

  boolean existsSynonymGroup(String id);

  boolean existsSynonymGroupCode(String locale, String code, String excludeId);

  long countSynonymTerms(String groupId);

  String insertSynonymGroup(String code, String locale, boolean active);

  void updateSynonymGroup(String id, String code, String locale, boolean active);

  void updateSynonymGroupActive(String id, boolean active);

  String insertSynonymTerm(String groupId, String term, String normalizedTerm);

  boolean existsSynonymTerm(String groupId, String normalizedTerm, String excludeId);

  boolean existsSynonymTermId(String termId);

  void deleteSynonymTerm(String termId);

  List<TopQueryResponse> findTopQueries(LocalDateTime from, LocalDateTime to, String locale, int limit);

  Page<ZeroResultQueryResponse> findZeroResultQueries(LocalDateTime from, LocalDateTime to, String locale, AdminPageRequest pageRequest);

  List<TopClickedProductResponse> findTopClickedProducts(LocalDateTime from, LocalDateTime to, String locale, int limit);

  ProjectionHealthSummaryResponse getProjectionHealthSummary();
  
  Double findAverageSearchLatencyMs(LocalDateTime from, LocalDateTime to);

  LocalDateTime findLatestProjectionSyncAt();

  Optional<SearchPreviewResponse> findSearchPreview(String productId);
}
