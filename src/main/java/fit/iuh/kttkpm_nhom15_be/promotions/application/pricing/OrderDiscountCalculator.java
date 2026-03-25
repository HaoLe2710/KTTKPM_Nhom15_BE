package fit.iuh.kttkpm_nhom15_be.promotions.application.pricing;

import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class OrderDiscountCalculator implements PromotionCalculator {
    @Override
    public boolean isApplicable(Promotion promotion, OrderCartDTO cart) {
        BigDecimal minOrderValue = PromotionConfigUtils.getBigDecimal(promotion.getConfig(), "minOrderValue");
        return minOrderValue == null || cart.subtotal().compareTo(minOrderValue) >= 0;
    }

    @Override
    public BigDecimal calculateDiscount(Promotion promotion, OrderCartDTO cart) {
        BigDecimal discountPercent = PromotionConfigUtils.getBigDecimal(promotion.getConfig(), "discountPercent");
        BigDecimal discountAmount = PromotionConfigUtils.getBigDecimal(promotion.getConfig(), "discountAmount");

        if (discountPercent != null) {
            return cart.subtotal()
                .multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return discountAmount == null ? BigDecimal.ZERO : discountAmount;
    }

    @Override
    public PromotionType getSupportedType() {
        return PromotionType.ORDER_DISCOUNT;
    }
}
