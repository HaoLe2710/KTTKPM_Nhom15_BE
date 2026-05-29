package fit.iuh.kttkpm_nhom15_be.search.application.usecases.query;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchFacetDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchProductsRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchRedirectDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchResponseDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.models.SearchQueryContext;
import fit.iuh.kttkpm_nhom15_be.search.application.results.SearchPageResult;
import fit.iuh.kttkpm_nhom15_be.search.application.support.SearchNormalizer;
import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchReadRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchProductsUseCase {

  private static final String DEFAULT_LOCALE = "vi";

  private final SearchReadRepository searchReadRepository;

  public SearchResponseDTO execute(SearchProductsRequest request) {
    Instant startedAt = Instant.now();
    SearchQueryContext queryContext = buildQueryContext(request);

    SearchRedirectDTO redirect = searchReadRepository.findRedirect(queryContext.locale(), queryContext.normalizedQuery());
    if (redirect != null) {
      searchReadRepository.logQuery(
        request.query() == null ? "" : request.query(),
        queryContext.normalizedQuery(),
        0,
        Duration.between(startedAt, Instant.now()).toMillis()
      );
      return new SearchResponseDTO(
        List.of(),
        List.of(),
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

    long latency = Duration.between(startedAt, Instant.now()).toMillis();
    searchReadRepository.logQuery(request.query() == null ? "" : request.query(), queryContext.normalizedQuery(), Math.toIntExact(result.total()), latency);
    if (result.total() == 0 && request.query() != null && !request.query().isBlank()) {
      searchReadRepository.upsertZeroResultQuery(request.query(), queryContext.normalizedQuery());
    }

    return new SearchResponseDTO(
      result.items(),
      facets,
      request.page(),
      request.size(),
      result.total(),
      request.query(),
      queryContext.normalizedQuery(),
      null
    );
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
