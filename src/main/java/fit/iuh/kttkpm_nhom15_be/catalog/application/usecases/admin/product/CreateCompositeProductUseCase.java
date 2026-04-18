package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product;

import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CompositeProductRequestDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CompositeProductRequestDTO.OptionAssignmentDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.CompositeProductRequestDTO.VariantRequestDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.events.CatalogProductChangedEvent;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Product;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Variant;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.VariantOption;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionValueRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductTypeRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CreateCompositeProductUseCase {

    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final OptionValueRepository optionValueRepository;
    private final VariantRepository variantRepository;
    private final VariantOptionRepository variantOptionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    public String execute(CompositeProductRequestDTO request) {
        if (!productTypeRepository.findById(request.getTypeId()).isPresent()) {
            throw new IllegalArgumentException("Khong tim thay product type: " + request.getTypeId());
        }

        Set<String> seenSkus = new HashSet<>();
        for (VariantRequestDTO variantRequest : request.getVariants()) {
            String normalizedSku = variantRequest.getSku().trim().toLowerCase();
            if (!seenSkus.add(normalizedSku)) {
                throw new IllegalArgumentException("SKU bi trung trong request: " + variantRequest.getSku());
            }
            if (variantRepository.existsBySku(variantRequest.getSku())) {
                throw new IllegalArgumentException("SKU da ton tai: " + variantRequest.getSku());
            }
        }

        // 1. Save Product
        Product p = Product.builder()
                .typeId(request.getTypeId())
                .name(request.getName().trim())
                .descriptionMd(request.getDescriptionMd())
                .isCustomizable(request.isCustomizable())
                .isActive(true)
                .slug(request.getName().trim().toLowerCase().replace(" ", "-") + "-" + System.currentTimeMillis())
                .build();
        
        Product savedProduct = productRepository.save(p);

        // 2 & 3. Save Variants & Variant Options
        if(request.getVariants() != null) {
            for (VariantRequestDTO vReq : request.getVariants()) {
                Variant v = Variant.builder()
                        .productId(savedProduct.getId())
                        .sku(vReq.getSku().trim())
                        .price(vReq.getPrice())
                        .stockQuantity(vReq.getStockQuantity())
                        .isActive(true)
                        .build();
                Variant savedVariant = variantRepository.save(v);

                if (vReq.getOptions() != null) {
                    for (OptionAssignmentDTO optReq : vReq.getOptions()) {
                        var optionValue = optionValueRepository.findById(optReq.getValueId())
                                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay option value: " + optReq.getValueId()));
                        if (optionValue.getOptionId() == null || !optionValue.getOptionId().equals(optReq.getOptionId())) {
                            throw new IllegalArgumentException("Option value khong thuoc option duoc chi dinh: " + optReq.getValueId());
                        }
                        VariantOption vo = VariantOption.builder()
                                .variantId(savedVariant.getId())
                                .optionValueId(optReq.getValueId())
                                .build();
                        variantOptionRepository.save(vo);
                    }
                }
            }
        }

        eventPublisher.publishEvent(new CatalogProductChangedEvent(
            savedProduct.getId(),
            "CATALOG_PRODUCT_CREATED",
            LocalDateTime.now()
        ));
        
        return savedProduct.getId();
    }
}
