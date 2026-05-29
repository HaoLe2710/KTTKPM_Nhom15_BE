package fit.iuh.kttkpm_nhom15_be.search.application.usecases.query;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchProductsRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchResponseDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.services.SearchProductsCacheService;
import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchReadRepository;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchProductsUseCase {

  private final SearchReadRepository searchReadRepository;
  private final SearchProductsCacheService searchProductsCacheService;

  public SearchResponseDTO execute(SearchProductsRequest request) {
    Instant startedAt = Instant.now();
    SearchResponseDTO response = searchProductsCacheService.search(request);

    String queryText = request.query() == null ? "" : request.query().trim();
    if (!queryText.isBlank()) {
      long latency = Duration.between(startedAt, Instant.now()).toMillis();
      searchReadRepository.logQuery(
        queryText,
        response.normalizedQuery(),
        Math.toIntExact(response.total()),
        latency
      );
      if (response.redirect() == null && response.total() == 0) {
        searchReadRepository.upsertZeroResultQuery(queryText, response.normalizedQuery());
      }
    }

    return response;
  }
}
