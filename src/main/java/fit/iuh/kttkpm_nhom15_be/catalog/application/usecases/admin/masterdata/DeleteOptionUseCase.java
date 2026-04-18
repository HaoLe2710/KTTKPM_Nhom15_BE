package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionValueRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantOptionRepository;
import lombok.RequiredArgsConstructor;
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
    public void execute(String id) {
        optionRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Khong tim thay option: " + id));

        var optionValues = optionValueRepository.findByOptionId(id);
        var optionValueIds = optionValues.stream().map(v -> v.getId()).collect(Collectors.toList());
        if (variantOptionRepository.existsByOptionValueIds(optionValueIds)) {
            throw new IllegalArgumentException("Khong the xoa option dang duoc su dung boi variants.");
        }

        optionValueRepository.deleteByOptionId(id);
        optionRepository.deleteById(id);
    }
}
