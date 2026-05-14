package fit.iuh.kttkpm_nhom15_be.search.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.ProductSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchFacetDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchProductsRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchRedirectDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchSuggestionDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.models.SearchQueryContext;
import fit.iuh.kttkpm_nhom15_be.search.application.results.SearchPageResult;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;

public interface SearchReadRepository {

  Page<ProductSummaryDTO> browseLegacy(String typeId, BigDecimal minPrice, BigDecimal maxPrice, int page, int size);

  SearchRedirectDTO findRedirect(String locale, String normalizedQuery);

  List<String> findSynonymTerms(String locale, Collection<String> normalizedTerms);

  SearchPageResult search(SearchProductsRequest request, SearchQueryContext queryContext);

  List<SearchFacetDTO> findFacets(SearchProductsRequest request, SearchQueryContext queryContext);

  List<SearchSuggestionDTO> findSuggestions(String normalizedQuery);

  void logQuery(String queryText, String normalizedQuery, int resultCount, long latencyMs);

  void upsertZeroResultQuery(String queryText, String normalizedQuery);
}
