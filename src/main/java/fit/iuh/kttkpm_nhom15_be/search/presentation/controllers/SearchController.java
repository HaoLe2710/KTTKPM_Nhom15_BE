package fit.iuh.kttkpm_nhom15_be.search.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchProductsRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchResponseDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchSuggestionDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.usecases.query.GetSearchSuggestionsUseCase;
import fit.iuh.kttkpm_nhom15_be.search.application.usecases.query.SearchProductsUseCase;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchController {

  private final SearchProductsUseCase searchProductsUseCase;
  private final GetSearchSuggestionsUseCase getSearchSuggestionsUseCase;

  @GetMapping("/api/v1/products/search")
  public ResponseEntity<SearchResponseDTO> searchProducts(
    @RequestParam(required = false) String q,
    @RequestParam(required = false) List<String> typeIds,
    @RequestParam(required = false) BigDecimal minPrice,
    @RequestParam(required = false) BigDecimal maxPrice,
    @RequestParam(required = false) Boolean inStock,
    @RequestParam(defaultValue = "relevance") String sort,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
  ) {
    return ResponseEntity.ok(searchProductsUseCase.execute(new SearchProductsRequest(q, typeIds, minPrice, maxPrice, inStock, sort, page, size)));
  }

  @GetMapping("/api/v1/search/suggestions")
  public ResponseEntity<List<SearchSuggestionDTO>> getSuggestions(@RequestParam String q) {
    return ResponseEntity.ok(getSearchSuggestionsUseCase.execute(q));
  }
}
