package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product;

import fit.iuh.kttkpm_nhom15_be.catalog.application.events.CatalogProductChangedEvent;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdateVariantPricingUseCase {

    private final VariantRepository variantRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Data
    public static class PatchVariantRequest {
        private BigDecimal price;
        private Integer addedStock;
    }

    @Transactional
    public void execute(String productId, String variantId, PatchVariantRequest req) {
        if (req.getPrice() == null && req.getAddedStock() == null) {
            throw new IllegalArgumentException("Phai cung cap it nhat mot truong can cap nhat: price hoac addedStock.");
        }

        var variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy variant: " + variantId));
        if (!productId.equals(variant.getProductId())) {
            throw new IllegalArgumentException("Variant không thuộc product được chỉ định.");
        }

        if (req.getPrice() != null) {
            if (req.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Price không được âm.");
            }
            variant.setPrice(req.getPrice());
        }

        if (req.getAddedStock() != null) {
            int newStock = variant.getStockQuantity() + req.getAddedStock();
            if (newStock < 0) {
                throw new IllegalArgumentException("Không được trừ stock vượt quá tồn kho hiện tại.");
            }
            variant.setStockQuantity(newStock);
        }

        variantRepository.save(variant);
        eventPublisher.publishEvent(new CatalogProductChangedEvent(
            productId,
            "CATALOG_VARIANT_UPDATED",
            LocalDateTime.now()
        ));
    }
}
