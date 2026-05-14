package fit.iuh.kttkpm_nhom15_be.promotions.application.pricing;

import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;

import java.math.BigDecimal;

public interface PromotionCalculator {
    boolean isApplicable(Promotion promotion, OrderCartDTO cart);
    BigDecimal calculateDiscount(Promotion promotion, OrderCartDTO cart);
    PromotionType getSupportedType();
}
