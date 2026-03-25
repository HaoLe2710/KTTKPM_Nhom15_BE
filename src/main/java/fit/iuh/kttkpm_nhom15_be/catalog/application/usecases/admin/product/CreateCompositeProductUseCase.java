package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CompositeProductRequestDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CompositeProductRequestDTO.OptionAssignmentDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CompositeProductRequestDTO.VariantRequestDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Product;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Variant;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.VariantOption;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCompositeProductUseCase {

    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;
    private final VariantOptionRepository variantOptionRepository;

    @Transactional(rollbackFor = Exception.class)
    public String execute(CompositeProductRequestDTO request) {
        // 1. Save Product
        Product p = Product.builder()
                .typeId(request.getTypeId())
                .name(request.getName())
                .descriptionMd(request.getDescriptionMd())
                .isCustomizable(request.isCustomizable())
                .isActive(true)
                .slug(request.getName().toLowerCase().replace(" ", "-") + "-" + System.currentTimeMillis())
                .build();
        
        Product savedProduct = productRepository.save(p);

        // 2 & 3. Save Variants & Variant Options
        if(request.getVariants() != null) {
            for (VariantRequestDTO vReq : request.getVariants()) {
                Variant v = Variant.builder()
                        .productId(savedProduct.getId())
                        .sku(vReq.getSku())
                        .price(vReq.getPrice())
                        .stockQuantity(vReq.getStockQuantity())
                        .isActive(true)
                        .build();
                Variant savedVariant = variantRepository.save(v);

                if (vReq.getOptions() != null) {
                    for (OptionAssignmentDTO optReq : vReq.getOptions()) {
                        VariantOption vo = VariantOption.builder()
                                .variantId(savedVariant.getId())
                                .optionValueId(optReq.getValueId())
                                .build();
                        variantOptionRepository.save(vo);
                    }
                }
            }
        }
        
        return savedProduct.getId();
    }
}
