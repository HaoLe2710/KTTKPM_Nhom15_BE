package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.ProductSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ListProductsSummaryUseCase {

    private final ProductRepository productRepository;

    public Page<ProductSummaryDTO> execute(String typeId, BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        return productRepository.findProductsSummary(typeId, minPrice, maxPrice, page, size);
    }
}
