package fit.iuh.kttkpm_nhom15_be.search.application.usecases.query;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.ProductSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.search.domain.repositories.SearchReadRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BrowseLegacyProductsUseCase {

  private final SearchReadRepository searchReadRepository;

  public Page<ProductSummaryDTO> execute(String typeId, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
    return searchReadRepository.browseLegacy(typeId, minPrice, maxPrice, page, size);
  }
}
