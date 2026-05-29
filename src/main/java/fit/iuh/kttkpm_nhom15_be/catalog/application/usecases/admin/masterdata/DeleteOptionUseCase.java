package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionValueRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantOptionRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeleteOptionUseCase {

    private final OptionRepository optionRepository;
    private final OptionValueRepository optionValueRepository;
    private final VariantOptionRepository variantOptionRepository;

    @Transactional
    @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
    public void execute(String id) {
        optionRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Không tìm thấy option: " + id));

        var optionValues = optionValueRepository.findByOptionId(id);
        var optionValueIds = optionValues.stream().map(v -> v.getId()).collect(Collectors.toList());
        if (variantOptionRepository.existsByOptionValueIds(optionValueIds)) {
            throw new IllegalArgumentException("Không thể xóa option đang được sử dụng bởi variants.");
        }

        optionValueRepository.deleteByOptionId(id);
        optionRepository.deleteById(id);
    }
}
