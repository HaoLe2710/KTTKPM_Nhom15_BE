package fit.iuh.kttkpm_nhom15_be.search.application.usecases.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchProductsRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchRedirectDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.results.SearchPageResult;
import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchReadRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SearchProductsUseCaseTest {

  @Test
  void searchShortCircuitsOnRedirectRule() {
    SearchReadRepository searchReadRepository = Mockito.mock(SearchReadRepository.class);
    SearchProductsUseCase useCase = new SearchProductsUseCase(searchReadRepository);

    when(searchReadRepository.findRedirect("vi", "son moi")).thenReturn(new SearchRedirectDTO("collection", "lipsticks"));

    var response = useCase.execute(new SearchProductsRequest("Sơn môi", List.of(), null, null, null, "relevance", 0, 20));

    assertNotNull(response.redirect());
    assertEquals("collection", response.redirect().type());
    verify(searchReadRepository, never()).search(any(), any());
  }

  @Test
  void searchLogsZeroResultQueries() {
    SearchReadRepository searchReadRepository = Mockito.mock(SearchReadRepository.class);
    SearchProductsUseCase useCase = new SearchProductsUseCase(searchReadRepository);

    when(searchReadRepository.findRedirect("vi", "serum")).thenReturn(null);
    when(searchReadRepository.findSynonymTerms(any(), any())).thenReturn(List.of());
    when(searchReadRepository.search(any(), any())).thenReturn(new SearchPageResult(List.of(), 0));
    when(searchReadRepository.findFacets(any(), any())).thenReturn(List.of());

    var response = useCase.execute(new SearchProductsRequest("serum", List.of(), BigDecimal.ZERO, null, null, "relevance", 0, 20));

    assertEquals(0, response.total());
    verify(searchReadRepository).upsertZeroResultQuery("serum", "serum");
  }
}
