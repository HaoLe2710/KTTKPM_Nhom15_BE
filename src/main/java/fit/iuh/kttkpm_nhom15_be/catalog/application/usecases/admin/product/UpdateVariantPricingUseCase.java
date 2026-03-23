package fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.product;

import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UpdateVariantPricingUseCase {

    private final VariantRepository variantRepository;

    @Data
    public static class PatchVariantRequest {
        private BigDecimal price;
        private int addedStock;
    }

    @Transactional
    public void execute(String variantId, PatchVariantRequest req) {
        if(req.getPrice() != null) {
            variantRepository.patchPriceAndStock(variantId, req.getPrice(), req.getAddedStock());
        } else {
            // Ideally should fetch existing price if not provided, but mostly Admin provides both or we use a more dynamic query.
            // For simplicity in this architectural demo, we assume both are sent or we handle it inside Repository cleanly.
            throw new IllegalArgumentException("Price must be explicitly provided in this payload version.");
        }
    }
}
