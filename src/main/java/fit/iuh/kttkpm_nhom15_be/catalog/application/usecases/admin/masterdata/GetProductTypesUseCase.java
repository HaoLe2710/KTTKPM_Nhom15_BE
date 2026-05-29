package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.ProductTypeResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductTypeRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetProductTypesUseCase {

    private final ProductTypeRepository repository;

    @Cacheable(
            cacheNames = CacheNames.PRODUCT_MASTER_DATA,
            key = "T(fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheKeys).masterData('product-types')"
    )
    public List<ProductTypeResponse> execute() {
        return repository.findAll().stream()
                .map(pt -> ProductTypeResponse.builder()
                        .id(pt.getId())
                        .code(pt.getCode())
                        .name(pt.getName())
                        .isActive(true)
                        .build())
                .collect(Collectors.toList());
    }
}
