package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductTypeRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteProductTypeUseCase {

    private final ProductTypeRepository productTypeRepository;
    private final ProductRepository productRepository;

    @Transactional
    @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
    public void execute(String id) {
        productTypeRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Không tìm thấy product type: " + id));

        if (productRepository.existsByTypeId(id)) {
            throw new IllegalArgumentException("Không thể xóa product type đang được sử dụng bởi sản phẩm.");
        }

        productTypeRepository.deleteById(id);
    }
}
