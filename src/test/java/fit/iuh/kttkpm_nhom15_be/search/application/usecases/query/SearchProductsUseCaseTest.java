package fit.iuh.kttkpm_nhom15_be.search.application.usecases.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchProductsRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchRedirectDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchResponseDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.services.SearchProductsCacheService;
import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchReadRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SearchProductsUseCaseTest {

  @Test
  void searchShortCircuitsOnRedirectRule() {
    SearchReadRepository searchReadRepository = Mockito.mock(SearchReadRepository.class);
    SearchProductsCacheService searchProductsCacheService = Mockito.mock(SearchProductsCacheService.class);
    SearchProductsUseCase useCase = new SearchProductsUseCase(searchReadRepository, searchProductsCacheService);
    SearchProductsRequest request = new SearchProductsRequest("Son moi", List.of(), null, null, null, "relevance", 0, 20);

    when(searchProductsCacheService.search(request)).thenReturn(new SearchResponseDTO(
      List.of(),
      List.of(),
      0,
      20,
      0,
      "Son moi",
      "son moi",
      new SearchRedirectDTO("collection", "lipsticks")
    ));

    var response = useCase.execute(request);

    assertNotNull(response.redirect());
    assertEquals("collection", response.redirect().type());
    verify(searchReadRepository, never()).search(any(), any());
    verify(searchReadRepository, never()).upsertZeroResultQuery(any(), any());
  }

  @Test
  void searchLogsZeroResultQueries() {
    SearchReadRepository searchReadRepository = Mockito.mock(SearchReadRepository.class);
    SearchProductsCacheService searchProductsCacheService = Mockito.mock(SearchProductsCacheService.class);
    SearchProductsUseCase useCase = new SearchProductsUseCase(searchReadRepository, searchProductsCacheService);
    SearchProductsRequest request = new SearchProductsRequest("serum", List.of(), BigDecimal.ZERO, null, null, "relevance", 0, 20);

    when(searchProductsCacheService.search(request)).thenReturn(new SearchResponseDTO(
      List.of(),
      List.of(),
      0,
      20,
      0,
      "serum",
      "serum",
      null
    ));

    var response = useCase.execute(request);

    assertEquals(0, response.total());
    verify(searchReadRepository).upsertZeroResultQuery("serum", "serum");
  }
}
