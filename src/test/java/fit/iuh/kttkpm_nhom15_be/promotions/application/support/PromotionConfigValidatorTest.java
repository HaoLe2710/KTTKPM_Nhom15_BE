package fit.iuh.kttkpm_nhom15_be.promotions.application.support;

import fit.iuh.kttkpm_nhom15_be.catalog.application.interfaces.CatalogFacade;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

class PromotionConfigValidatorTest {

    private final CatalogFacade catalogFacade = Mockito.mock(CatalogFacade.class);
    private final PromotionConfigValidator validator = new PromotionConfigValidator(catalogFacade);

    @Test
    void productDiscountValidationFailsWhenAnyVariantIsMissing() {
        when(catalogFacade.checkVariantsExist(List.of("v1", "v2"))).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> validator.validateForSave(
            PromotionType.PRODUCT_DISCOUNT,
            Map.of("variantIds", List.of("v1", "v2"), "discountPercent", 10),
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        ));
    }

    @Test
    void buyXGetYValidationFailsWhenReferencedVariantIsMissing() {
        when(catalogFacade.checkVariantsExist(List.of("v1", "v2"))).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> validator.validateForSave(
            PromotionType.BUY_X_GET_Y,
            Map.of("buyVariantId", "v1", "buyQuantity", 2, "getVariantId", "v2", "getQuantity", 1),
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        ));
    }

    @Test
    void validProductDiscountPassesValidation() {
        when(catalogFacade.checkVariantsExist(anyList())).thenReturn(true);

        assertDoesNotThrow(() -> validator.validateForSave(
            PromotionType.PRODUCT_DISCOUNT,
            Map.of("variantIds", List.of("v1"), "discountAmount", 20),
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        ));
    }

    @Test
    void orderDiscountValidationRequiresTargetCustomers() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateForSave(
            PromotionType.ORDER_DISCOUNT,
            Map.of("discountAmount", 20, "minOrderValue", 0),
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        ));
    }

    @Test
    void validOrderDiscountWithTargetCustomersPassesValidation() {
        assertDoesNotThrow(() -> validator.validateForSave(
            PromotionType.ORDER_DISCOUNT,
            Map.of("discountAmount", 20, "minOrderValue", 0, "targetCustomerIds", List.of("user-1")),
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1),
            10
        ));
    }
}
