package fit.iuh.kttkpm_nhom15_be.catalog.application.services;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.OptionValueListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CatalogAdminDtos.SemanticMasterListItemResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.SemanticMasterKind;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.CatalogAdminRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest;
import fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheNames;
import fit.iuh.kttkpm_nhom15_be.shared.application.cache.CachedPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogMasterDataCacheService {

  private final CatalogAdminRepository catalogAdminRepository;

  @Cacheable(
    cacheNames = CacheNames.PRODUCT_MASTER_DATA,
    key = "T(fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheKeys).adminMasterData('admin-options', #active, #pageRequest)"
  )
  public CachedPage<OptionListItemResponse> getOptions(Boolean active, AdminPageRequest pageRequest) {
    try {
      return CachedPage.from(catalogAdminRepository.findOptions(active, pageRequest));
    } catch (DataAccessException ex) {
      log.warn("Cannot load options (likely migration/schema not ready): {}", ex.getMessage());
      return CachedPage.empty(pageRequest.page(), pageRequest.size());
    }
  }

  @Cacheable(
    cacheNames = CacheNames.PRODUCT_MASTER_DATA,
    key = "T(fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheKeys).adminMasterData('admin-option-values:' + (#optionId == null ? 'all' : #optionId), #active, #pageRequest)"
  )
  public CachedPage<OptionValueListItemResponse> getOptionValues(String optionId, Boolean active, AdminPageRequest pageRequest) {
    try {
      return CachedPage.from(catalogAdminRepository.findOptionValues(optionId, active, pageRequest));
    } catch (DataAccessException ex) {
      log.warn("Cannot load option values (likely migration/schema not ready): {}", ex.getMessage());
      return CachedPage.empty(pageRequest.page(), pageRequest.size());
    }
  }

  @Cacheable(
    cacheNames = CacheNames.PRODUCT_MASTER_DATA,
    key = "T(fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheKeys).adminMasterData('admin-semantic:' + #kind.name(), #active, #pageRequest)"
  )
  public CachedPage<SemanticMasterListItemResponse> getSemanticMasters(SemanticMasterKind kind,
                                                                       Boolean active,
                                                                       AdminPageRequest pageRequest) {
    try {
      return CachedPage.from(catalogAdminRepository.findSemanticMasters(kind, active, pageRequest));
    } catch (DataAccessException ex) {
      log.warn("Cannot load semantic masters for {} (likely migration/schema not ready): {}", kind, ex.getMessage());
      return CachedPage.empty(pageRequest.page(), pageRequest.size());
    }
  }
}
