package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.*;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Option;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.OptionValue;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionValueRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetOptionsUseCase {

    private final OptionRepository optionRepository;
    private final OptionValueRepository optionValueRepository;

    @Cacheable(
            cacheNames = CacheNames.PRODUCT_MASTER_DATA,
            key = "T(fit.iuh.kttkpm_nhom15_be.shared.application.cache.CacheKeys).masterData('options')"
    )
    public List<OptionResponse> execute() {
        List<Option> options = optionRepository.findAll();
        List<OptionValue> allValues = optionValueRepository.findAll();
        
        Map<String, List<OptionValue>> valuesByOptionId = allValues.stream()
                .collect(Collectors.groupingBy(OptionValue::getOptionId));

        return options.stream().map(opt -> {
            List<OptionValue> vals = valuesByOptionId.getOrDefault(opt.getId(), List.of());
            
            List<OptionValueResponse> valDTOs = vals.stream()
                    .filter(OptionValue::isActive)
                    .map(v -> OptionValueResponse.builder()
                        .id(v.getId())
                        .value(v.getValue())
                        .build())
                    .collect(Collectors.toList());
                    
            return OptionResponse.builder()
                    .id(opt.getId())
                    .code(opt.getCode())
                    .name(opt.getName())
                    .values(valDTOs)
                    .build();
        }).collect(Collectors.toList());
    }
}
