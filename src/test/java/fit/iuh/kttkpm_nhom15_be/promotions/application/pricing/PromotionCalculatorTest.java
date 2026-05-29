package fit.iuh.kttkpm_nhom15_be.promotions.application.pricing;

import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartItemDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromotionCalculatorTest {

    private final OrderDiscountCalculator orderDiscountCalculator = new OrderDiscountCalculator();
    private final ProductDiscountCalculator productDiscountCalculator = new ProductDiscountCalculator();
    private final BuyXGetYCalculator buyXGetYCalculator = new BuyXGetYCalculator();

    @Test
    void orderDiscountPercentAppliesWhenSubtotalMeetsMinimum() {
        Promotion promotion = promotion(PromotionType.ORDER_DISCOUNT, Map.of(
            "minOrderValue", 100,
            "discountPercent", 10
        ));
        OrderCartDTO cart = cart(BigDecimal.valueOf(200), List.of(item("v1", 2, 100)));

        assertTrue(orderDiscountCalculator.isApplicable(promotion, cart));
        assertEquals(BigDecimal.valueOf(20.00).setScale(2), orderDiscountCalculator.calculateDiscount(promotion, cart));
    }

    @Test
    void orderDiscountPercentIsCappedByMaxDiscountAmount() {
        Promotion promotion = promotion(PromotionType.ORDER_DISCOUNT, Map.of(
            "minOrderValue", 100,
            "discountPercent", 20,
            "maxDiscountAmount", 30
        ));
        OrderCartDTO cart = cart(BigDecimal.valueOf(500), List.of(item("v1", 5, 100)));

        assertEquals(BigDecimal.valueOf(30.0), orderDiscountCalculator.calculateDiscount(promotion, cart));
    }

    @Test
    void orderDiscountFixedAmountReturnsConfiguredDiscount() {
        Promotion promotion = promotion(PromotionType.ORDER_DISCOUNT, Map.of(
            "minOrderValue", 0,
            "discountAmount", 30
        ));

        assertEquals(BigDecimal.valueOf(30.0), orderDiscountCalculator.calculateDiscount(promotion, cart(BigDecimal.valueOf(120), List.of(item("v1", 1, 120)))));
    }

    @Test
    void productDiscountOnlyAppliesToMatchingVariants() {
        Promotion promotion = promotion(PromotionType.PRODUCT_DISCOUNT, Map.of(
            "variantIds", List.of("v1"),
            "discountPercent", 10
        ));
        OrderCartDTO cart = cart(BigDecimal.valueOf(250), List.of(
            item("v1", 2, 100),
            item("v2", 1, 50)
        ));

        assertTrue(productDiscountCalculator.isApplicable(promotion, cart));
        assertEquals(BigDecimal.valueOf(20.00).setScale(2), productDiscountCalculator.calculateDiscount(promotion, cart));
    }

    @Test
    void productDiscountIsNotApplicableWhenNoTargetVariantExists() {
        Promotion promotion = promotion(PromotionType.PRODUCT_DISCOUNT, Map.of(
            "variantIds", List.of("v3"),
            "discountAmount", 15
        ));
        OrderCartDTO cart = cart(BigDecimal.valueOf(100), List.of(item("v1", 1, 100)));

        assertFalse(productDiscountCalculator.isApplicable(promotion, cart));
    }

    @Test
    void buyXGetYCalculatesFreeUnitsForExactQuantity() {
        Promotion promotion = promotion(PromotionType.BUY_X_GET_Y, Map.of(
            "buyVariantId", "v1",
            "buyQuantity", 2,
            "getVariantId", "v2",
            "getQuantity", 1
        ));
        OrderCartDTO cart = cart(BigDecimal.valueOf(250), List.of(
            item("v1", 2, 100),
            item("v2", 1, 50)
        ));

        assertTrue(buyXGetYCalculator.isApplicable(promotion, cart));
        assertEquals(BigDecimal.valueOf(50), buyXGetYCalculator.calculateDiscount(promotion, cart));
    }

    @Test
    void buyXGetYCapsDiscountByAvailableTargetQuantity() {
        Promotion promotion = promotion(PromotionType.BUY_X_GET_Y, Map.of(
            "buyVariantId", "v1",
            "buyQuantity", 2,
            "getVariantId", "v2",
            "getQuantity", 1
        ));
        OrderCartDTO cart = cart(BigDecimal.valueOf(450), List.of(
            item("v1", 4, 100),
            item("v2", 1, 50)
        ));

        assertEquals(BigDecimal.valueOf(50), buyXGetYCalculator.calculateDiscount(promotion, cart));
    }

    @Test
    void buyXGetYIsNotApplicableWhenQuantitiesAreInsufficient() {
        Promotion promotion = promotion(PromotionType.BUY_X_GET_Y, Map.of(
            "buyVariantId", "v1",
            "buyQuantity", 2,
            "getVariantId", "v2",
            "getQuantity", 1
        ));
        OrderCartDTO cart = cart(BigDecimal.valueOf(150), List.of(
            item("v1", 1, 100),
            item("v2", 1, 50)
        ));

        assertFalse(buyXGetYCalculator.isApplicable(promotion, cart));
    }

    private Promotion promotion(PromotionType type, Map<String, Object> config) {
        return Promotion.builder()
            .type(type)
            .config(config)
            .build();
    }

    private OrderCartDTO cart(BigDecimal subtotal, List<OrderCartItemDTO> items) {
        return new OrderCartDTO(subtotal, items);
    }

    private OrderCartItemDTO item(String variantId, int quantity, int unitPrice) {
        BigDecimal price = BigDecimal.valueOf(unitPrice);
        return new OrderCartItemDTO(
            variantId,
            quantity,
            price,
            price.multiply(BigDecimal.valueOf(quantity))
        );
    }
}
