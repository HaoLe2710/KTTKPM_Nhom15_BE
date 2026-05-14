package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.ProductTypeRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.ProductTypeResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProductTypeUseCase {

    private final ProductTypeRepository repository;

    @Transactional
    public ProductTypeResponse execute(String id, ProductTypeRequest request) {
        var existing = repository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Khong tim thay product type: " + id));

        String normalizedCode = request.getCode().trim();
        repository.findByCode(normalizedCode).ifPresent(found -> {
            if (!found.getId().equals(id)) {
                throw new IllegalArgumentException("Product type code da ton tai: " + request.getCode());
            }
        });

        existing.setCode(normalizedCode);
        existing.setName(request.getName().trim());

        var saved = repository.save(existing);
        return ProductTypeResponse.builder()
                .id(saved.getId())
                .code(saved.getCode())
                .name(saved.getName())
                .isActive(true)
                .build();
    }
}
