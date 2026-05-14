package fit.iuh.kttkpm_nhom15_be.promotions.application.pricing;

import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartItemDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

@Component
public class ProductDiscountCalculator implements PromotionCalculator {
    @Override
    public boolean isApplicable(Promotion promotion, OrderCartDTO cart) {
        Set<String> targetIds = Set.copyOf(PromotionConfigUtils.getStringList(promotion.getConfig(), "variantIds"));
        return cart.items().stream().map(OrderCartItemDTO::variantId).anyMatch(targetIds::contains);
    }

    @Override
    public BigDecimal calculateDiscount(Promotion promotion, OrderCartDTO cart) {
        Set<String> targetIds = Set.copyOf(PromotionConfigUtils.getStringList(promotion.getConfig(), "variantIds"));
        BigDecimal discountPercent = PromotionConfigUtils.getBigDecimal(promotion.getConfig(), "discountPercent");
        BigDecimal discountAmount = PromotionConfigUtils.getBigDecimal(promotion.getConfig(), "discountAmount");

        return cart.items().stream()
            .filter(item -> targetIds.contains(item.variantId()))
            .map(item -> calculateItemDiscount(item, discountPercent, discountAmount))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateItemDiscount(OrderCartItemDTO item, BigDecimal discountPercent, BigDecimal discountAmount) {
        if (discountPercent != null) {
            return item.lineTotal()
                .multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        if (discountAmount != null) {
            BigDecimal totalDiscount = discountAmount.multiply(BigDecimal.valueOf(item.quantity()));
            return totalDiscount.min(item.lineTotal());
        }
        return BigDecimal.ZERO;
    }

    @Override
    public PromotionType getSupportedType() {
        return PromotionType.PRODUCT_DISCOUNT;
    }
}
