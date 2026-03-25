package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.ProductTypeRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.ProductTypeResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.ProductType;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateProductTypeUseCase {

    private final ProductTypeRepository repository;

    public ProductTypeResponse execute(ProductTypeRequest request) {
        ProductType newType = ProductType.builder()
                .code(request.getCode())
                .name(request.getName())
                .build();
                
        ProductType saved = repository.save(newType);
        
        return ProductTypeResponse.builder()
                .id(saved.getId())
                .code(saved.getCode())
                .name(saved.getName())
                .build();
    }
}
