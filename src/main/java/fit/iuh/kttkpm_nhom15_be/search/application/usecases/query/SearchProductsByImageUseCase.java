package fit.iuh.kttkpm_nhom15_be.search.application.usecases.query;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.ImageSearchResponseDTO;
import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchProductsRequest;
import fit.iuh.kttkpm_nhom15_be.search.application.interfaces.ImageQueryExtractorPort;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchProductsByImageUseCase {

  private final ImageQueryExtractorPort imageQueryExtractorPort;
  private final SearchProductsUseCase searchProductsUseCase;

  public ImageSearchResponseDTO execute(byte[] imageBytes,
                                        String mimeType,
                                        List<String> typeIds,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice,
                                        Boolean inStock,
                                        String sort,
                                        int page,
                                        int size) {
    String extractedQuery = imageQueryExtractorPort.extractQuery(imageBytes, mimeType);
    if (extractedQuery == null || extractedQuery.isBlank()) {
      throw new ApiValidationException("Không thể phân tích ảnh để tìm sản phẩm.");
    }

    var result = searchProductsUseCase.execute(new SearchProductsRequest(
      extractedQuery,
      typeIds,
      minPrice,
      maxPrice,
      inStock,
      sort,
      page,
      size
    ));

    return new ImageSearchResponseDTO(extractedQuery, result);
  }
}

