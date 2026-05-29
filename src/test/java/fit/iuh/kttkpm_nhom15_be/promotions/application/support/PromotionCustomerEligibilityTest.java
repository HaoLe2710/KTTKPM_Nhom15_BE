package fit.iuh.kttkpm_nhom15_be.promotions.application.support;

import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromotionCustomerEligibilityTest {

    @Test
    void assignedCustomerCanUseVoucher() {
        Promotion promotion = Promotion.builder()
            .config(Map.of("targetCustomerIds", List.of("user-1", "user-2")))
            .build();

        assertTrue(PromotionCustomerEligibility.isEligibleForUser(promotion, "user-1"));
    }

    @Test
    void unassignedOrGuestCustomerCannotUseVoucher() {
        Promotion promotion = Promotion.builder()
            .config(Map.of("targetCustomerIds", List.of("user-1")))
            .build();

        assertFalse(PromotionCustomerEligibility.isEligibleForUser(promotion, "user-2"));
        assertFalse(PromotionCustomerEligibility.isEligibleForUser(promotion, "guest-123"));
        assertFalse(PromotionCustomerEligibility.isEligibleForUser(promotion, null));
    }
}
