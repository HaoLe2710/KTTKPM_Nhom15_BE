package fit.iuh.kttkpm_nhom15_be.promotions.application.pricing;

import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartItemDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class BuyXGetYCalculator implements PromotionCalculator {
    @Override
    public boolean isApplicable(Promotion promotion, OrderCartDTO cart) {
        String buyVariantId = PromotionConfigUtils.getString(promotion.getConfig(), "buyVariantId");
        String getVariantId = PromotionConfigUtils.getString(promotion.getConfig(), "getVariantId");
        Integer buyQuantity = PromotionConfigUtils.getInteger(promotion.getConfig(), "buyQuantity");
        Integer getQuantity = PromotionConfigUtils.getInteger(promotion.getConfig(), "getQuantity");

        int buyCount = totalQuantity(cart, buyVariantId);
        int getCount = totalQuantity(cart, getVariantId);

        return buyQuantity != null && getQuantity != null && buyCount >= buyQuantity && getCount >= getQuantity;
    }

    @Override
    public BigDecimal calculateDiscount(Promotion promotion, OrderCartDTO cart) {
        String buyVariantId = PromotionConfigUtils.getString(promotion.getConfig(), "buyVariantId");
        String getVariantId = PromotionConfigUtils.getString(promotion.getConfig(), "getVariantId");
        Integer buyQuantity = PromotionConfigUtils.getInteger(promotion.getConfig(), "buyQuantity");
        Integer getQuantity = PromotionConfigUtils.getInteger(promotion.getConfig(), "getQuantity");

        if (buyQuantity == null || getQuantity == null) {
            return BigDecimal.ZERO;
        }

        int freeUnits = Math.min((totalQuantity(cart, buyVariantId) / buyQuantity) * getQuantity, totalQuantity(cart, getVariantId));
        if (freeUnits <= 0) {
            return BigDecimal.ZERO;
        }

        OrderCartItemDTO targetItem = findItem(cart, getVariantId)
            .orElseThrow(() -> new IllegalArgumentException("Missing target item for BUY_X_GET_Y"));
        return targetItem.unitPrice().multiply(BigDecimal.valueOf(freeUnits));
    }

    private int totalQuantity(OrderCartDTO cart, String variantId) {
        return cart.items().stream()
            .filter(item -> item.variantId().equals(variantId))
            .mapToInt(OrderCartItemDTO::quantity)
            .sum();
    }

    private Optional<OrderCartItemDTO> findItem(OrderCartDTO cart, String variantId) {
        return cart.items().stream().filter(item -> item.variantId().equals(variantId)).findFirst();
    }

    @Override
    public PromotionType getSupportedType() {
        return PromotionType.BUY_X_GET_Y;
    }
}
