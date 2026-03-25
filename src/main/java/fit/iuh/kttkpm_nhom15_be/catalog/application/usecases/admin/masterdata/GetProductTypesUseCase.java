package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.ProductTypeResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetProductTypesUseCase {

    private final ProductTypeRepository repository;

    public List<ProductTypeResponse> execute() {
        return repository.findAll().stream()
                .map(pt -> ProductTypeResponse.builder()
                        .id(pt.getId())
                        .code(pt.getCode())
                        .name(pt.getName())
                        .build())
                .collect(Collectors.toList());
    }
}
