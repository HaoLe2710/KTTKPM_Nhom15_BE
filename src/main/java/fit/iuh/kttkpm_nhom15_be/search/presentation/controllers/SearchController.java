package fit.iuh.kttkpm_nhom15_be.search.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.ImageSearchResponseDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchProductsRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchResponseDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchSuggestionDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.usecases.query.GetSearchSuggestionsUseCase;
import fit.iuh.kttkpm_nhom15_be.search.application.usecases.query.SearchProductsByImageUseCase;
import fit.iuh.kttkpm_nhom15_be.search.application.usecases.query.SearchProductsUseCase;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class SearchController {

  private final SearchProductsUseCase searchProductsUseCase;
  private final SearchProductsByImageUseCase searchProductsByImageUseCase;
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

  @PostMapping("/api/v1/products/search/image")
  public ResponseEntity<ImageSearchResponseDTO> searchProductsByImage(
    @RequestParam("file") MultipartFile file,
    @RequestParam(required = false) List<String> typeIds,
    @RequestParam(required = false) BigDecimal minPrice,
    @RequestParam(required = false) BigDecimal maxPrice,
    @RequestParam(required = false) Boolean inStock,
    @RequestParam(defaultValue = "relevance") String sort,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
  ) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new ApiValidationException("Vui lòng tải ảnh để tìm kiếm.");
    }
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new ApiValidationException("File tải lên phải là hình ảnh.");
    }

    return ResponseEntity.ok(searchProductsByImageUseCase.execute(
      file.getBytes(),
      contentType,
      typeIds,
      minPrice,
      maxPrice,
      inStock,
      sort,
      page,
      size
    ));
  }
}
