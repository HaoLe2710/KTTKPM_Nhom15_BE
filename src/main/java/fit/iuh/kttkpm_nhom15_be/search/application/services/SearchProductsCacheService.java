package fit.iuh.kttkpm_nhom15_be.search.application.services;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchFacetDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchProductsRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchRedirectDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchResponseDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.models.SearchQueryContext;
import fit.iuh.kttkpm_nhom15_be.search.application.results.SearchPageResult;
import fit.iuh.kttkpm_nhom15_be.search.application.support.SearchNormalizer;
import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchReadRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheNames;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchProductsCacheService {

  private static final String DEFAULT_LOCALE = "vi";

  private final SearchReadRepository searchReadRepository;

  @Cacheable(
    cacheNames = CacheNames.PRODUCT_SEARCH,
    key = "T(fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheKeys).productSearch(#request.query(), #request.typeIds(), #request.minPrice(), #request.maxPrice(), #request.inStock(), #request.sort(), #request.page(), #request.size())"
  )
  public SearchResponseDTO search(SearchProductsRequest request) {
    SearchQueryContext queryContext = buildQueryContext(request);

    SearchRedirectDTO redirect = searchReadRepository.findRedirect(queryContext.locale(), queryContext.normalizedQuery());
    if (redirect != null) {
      return new SearchResponseDTO(
        new ArrayList<>(),
        new ArrayList<>(),
        request.page(),
        request.size(),
        0,
        request.query(),
        queryContext.normalizedQuery(),
        redirect
      );
    }

    SearchPageResult result = searchReadRepository.search(request, queryContext);
    List<SearchFacetDTO> facets = searchReadRepository.findFacets(request, queryContext);

    return new SearchResponseDTO(
      mutableList(result.items()),
      mutableList(facets),
      request.page(),
      request.size(),
      result.total(),
      request.query(),
      queryContext.normalizedQuery(),
      null
    );
  }

  private <T> List<T> mutableList(List<T> values) {
    return values == null ? new ArrayList<>() : new ArrayList<>(values);
  }

  private SearchQueryContext buildQueryContext(SearchProductsRequest request) {
    String normalizedQuery = SearchNormalizer.normalizeText(request.query());
    String rawLowerQuery = request.query() == null ? "" : request.query().trim().toLowerCase();
    String normalizedSku = SearchNormalizer.normalizeSku(request.query());
    List<String> expandedTerms = expandDictionaryTerms(normalizedQuery);
    return new SearchQueryContext(DEFAULT_LOCALE, normalizedQuery, rawLowerQuery, normalizedSku, expandedTerms);
  }

  private List<String> expandDictionaryTerms(String normalizedQuery) {
    if (normalizedQuery == null || normalizedQuery.isBlank()) {
      return List.of();
    }

    LinkedHashSet<String> expanded = new LinkedHashSet<>();
    expanded.add(normalizedQuery);
    expanded.addAll(SearchNormalizer.tokenize(normalizedQuery));
    expanded.addAll(searchReadRepository.findSynonymTerms(DEFAULT_LOCALE, expanded));
    return new ArrayList<>(expanded);
  }
}
