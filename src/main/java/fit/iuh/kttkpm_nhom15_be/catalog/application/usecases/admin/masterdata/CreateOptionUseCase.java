package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.*;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Option;
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
public class CreateOptionUseCase {

    private final OptionRepository optionRepository;
    private final OptionValueRepository optionValueRepository;

    @Transactional
    @CacheEvict(cacheNames = CacheNames.PRODUCT_MASTER_DATA, allEntries = true)
    public OptionResponse execute(OptionRequest request) {
        if (optionRepository.existsByCode(request.getCode().trim())) {
            throw new IllegalArgumentException("Option code da ton tai: " + request.getCode());
        }

        Option newOption = Option.builder()
                .code(request.getCode().trim())
                .name(request.getName().trim())
                .build();
                
        Option savedOption = optionRepository.save(newOption);
        
        List<OptionValueResponse> valueResponses = new ArrayList<>();
        
        if (request.getValues() != null && !request.getValues().isEmpty()) {
            for (String valStr : request.getValues()) {
                if (valStr == null || valStr.trim().isEmpty()) {
                    throw new IllegalArgumentException("Giá trị option không được để trống");
                }
                OptionValue ov = OptionValue.builder()
                        .optionId(savedOption.getId())
                        .value(valStr.trim())
                        .isActive(true)
                        .build();
                OptionValue savedOv = optionValueRepository.save(ov);
                valueResponses.add(OptionValueResponse.builder()
                        .id(savedOv.getId())
                        .value(savedOv.getValue())
                        .build());
            }
        }
        
        return OptionResponse.builder()
                .id(savedOption.getId())
                .code(savedOption.getCode())
                .name(savedOption.getName())
                .values(valueResponses)
                .build();
    }
}
