package fit.iuh.kttkpm_nhom15_be.search.application.usecases.query;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchSuggestionDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.support.SearchNormalizer;
import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchReadRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheNames;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetSearchSuggestionsUseCase {

  private final SearchReadRepository searchReadRepository;

  @Cacheable(
    cacheNames = CacheNames.SEARCH_SUGGESTIONS,
    key = "T(fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheKeys).searchSuggestions(#query)",
    condition = "#query != null && !#query.isBlank()"
  )
  public List<SearchSuggestionDTO> execute(String query) {
    String normalizedQuery = SearchNormalizer.normalizeText(query);
    if (normalizedQuery == null || normalizedQuery.isBlank()) {
      return List.of();
    }
    return searchReadRepository.findSuggestions(normalizedQuery);
  }
}
