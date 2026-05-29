package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.OptionRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.OptionResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.OptionValueResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.OptionValue;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionValueRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UpdateOptionUseCase {

    private final OptionRepository optionRepository;
    private final OptionValueRepository optionValueRepository;

    @Transactional
    @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
    public OptionResponse execute(String id, OptionRequest request) {
        var existing = optionRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Không tìm thấy option: " + id));

        String normalizedCode = request.getCode().trim();
        optionRepository.findByCode(normalizedCode).ifPresent(found -> {
            if (!found.getId().equals(id)) {
                throw new IllegalArgumentException("Option code da ton tai: " + request.getCode());
            }
        });

        existing.setCode(normalizedCode);
        existing.setName(request.getName().trim());
        var savedOption = optionRepository.save(existing);

        if (request.getValues() != null) {
            optionValueRepository.deleteByOptionId(id);
            for (String rawValue : request.getValues()) {
                if (rawValue == null || rawValue.trim().isEmpty()) {
                    throw new IllegalArgumentException("Giá trị option không được để trống");
                }
                optionValueRepository.save(OptionValue.builder()
                        .optionId(id)
                        .value(rawValue.trim())
                        .isActive(true)
                        .build());
            }
        }

        List<OptionValueResponse> values = new ArrayList<>();
        for (OptionValue value : optionValueRepository.findByOptionId(id)) {
            if (value.isActive()) {
                values.add(OptionValueResponse.builder()
                        .id(value.getId())
                        .value(value.getValue())
                        .build());
            }
        }

        return OptionResponse.builder()
                .id(savedOption.getId())
                .code(savedOption.getCode())
                .name(savedOption.getName())
                .values(values)
                .build();
    }
}
