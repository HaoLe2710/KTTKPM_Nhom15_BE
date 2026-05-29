package fit.iuh.kttkpm_nhom15_be.promotions.application.support;

import fit.iuh.kttkpm_nhom15_be.catalog.application.interfaces.CatalogFacade;
import fit.iuh.kttkpm_nhom15_be.promotions.application.pricing.PromotionConfigUtils;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PromotionConfigValidator {
    private final CatalogFacade catalogFacade;

    public void validateForSave(
        PromotionType type,
        Map<String, Object> config,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer usageLimit
    ) {
        if (type == null) {
            throw new IllegalArgumentException("Promotion type is required.");
        }
        if (config == null || config.isEmpty()) {
            throw new IllegalArgumentException("Promotion config is required.");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Promotion start and end dates are required.");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("Promotion end date must be after start date.");
        }
        if (usageLimit != null && usageLimit < 1) {
            throw new IllegalArgumentException("Usage limit must be greater than 0.");
        }

        switch (type) {
            case ORDER_DISCOUNT -> validateOrderDiscount(config);
            case PRODUCT_DISCOUNT -> validateProductDiscount(config);
            case BUY_X_GET_Y -> validateBuyXGetY(config);
        }
    }

    public String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Promotion code is required.");
        }
        return code.trim().toUpperCase();
    }

    private void validateProductDiscount(Map<String, Object> config) {
        validateDiscountShape(config, false);
        List<String> variantIds = PromotionConfigUtils.getStringList(config, "variantIds");
        if (variantIds.isEmpty()) {
            throw new IllegalArgumentException("PRODUCT_DISCOUNT requires variantIds.");
        }
        if (!catalogFacade.checkVariantsExist(variantIds)) {
            throw new IllegalArgumentException("One or more variantIds do not exist.");
        }
    }

    private void validateOrderDiscount(Map<String, Object> config) {
        validateDiscountShape(config, true);
        if (PromotionCustomerEligibility.targetCustomerIds(config).isEmpty()) {
            throw new IllegalArgumentException("ORDER_DISCOUNT requires targetCustomerIds.");
        }
    }

    private void validateBuyXGetY(Map<String, Object> config) {
        String buyVariantId = PromotionConfigUtils.getString(config, "buyVariantId");
        String getVariantId = PromotionConfigUtils.getString(config, "getVariantId");
        Integer buyQuantity = PromotionConfigUtils.getInteger(config, "buyQuantity");
        Integer getQuantity = PromotionConfigUtils.getInteger(config, "getQuantity");

        if (buyVariantId == null || buyVariantId.isBlank() || getVariantId == null || getVariantId.isBlank()) {
            throw new IllegalArgumentException("BUY_X_GET_Y requires buyVariantId and getVariantId.");
        }
        if (buyQuantity == null || buyQuantity < 1 || getQuantity == null || getQuantity < 1) {
            throw new IllegalArgumentException("BUY_X_GET_Y requires positive buyQuantity and getQuantity.");
        }
        if (!catalogFacade.checkVariantsExist(List.of(buyVariantId, getVariantId))) {
            throw new IllegalArgumentException("BUY_X_GET_Y references missing variants.");
        }
    }

    private void validateDiscountShape(Map<String, Object> config, boolean requireMinOrderValue) {
        BigDecimal discountPercent = PromotionConfigUtils.getBigDecimal(config, "discountPercent");
        BigDecimal discountAmount = PromotionConfigUtils.getBigDecimal(config, "discountAmount");
        BigDecimal maxDiscountAmount = PromotionConfigUtils.getBigDecimal(config, "maxDiscountAmount");
        boolean hasPercent = discountPercent != null;
        boolean hasAmount = discountAmount != null;

        if (hasPercent == hasAmount) {
            throw new IllegalArgumentException("Exactly one of discountPercent or discountAmount is required.");
        }
        if (discountPercent != null && (discountPercent.compareTo(BigDecimal.ZERO) <= 0 || discountPercent.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new IllegalArgumentException("discountPercent must be between 0 and 100.");
        }
        if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("discountAmount must be greater than 0.");
        }
        if (maxDiscountAmount != null && maxDiscountAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("maxDiscountAmount must be greater than 0.");
        }
        if (maxDiscountAmount != null && discountPercent == null) {
            throw new IllegalArgumentException("maxDiscountAmount can only be used with discountPercent.");
        }
        if (requireMinOrderValue) {
            BigDecimal minOrderValue = PromotionConfigUtils.getBigDecimal(config, "minOrderValue");
            if (minOrderValue == null || minOrderValue.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("ORDER_DISCOUNT requires minOrderValue >= 0.");
            }
        }
    }
}
