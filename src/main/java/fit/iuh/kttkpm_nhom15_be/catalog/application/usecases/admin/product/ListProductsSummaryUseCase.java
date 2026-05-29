package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.ProductSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.services.ProductListCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ListProductsSummaryUseCase {

    private final ProductListCacheService productListCacheService;

    public Page<ProductSummaryDTO> execute(String typeId, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        return productListCacheService.getProductSummaries(typeId, minPrice, maxPrice, page, size).toPage();
    }
}
