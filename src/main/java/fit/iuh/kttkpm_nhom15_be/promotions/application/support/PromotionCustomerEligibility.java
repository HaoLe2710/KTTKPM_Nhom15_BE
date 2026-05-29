package fit.iuh.kttkpm_nhom15_be.promotions.application.support;

import fit.iuh.kttkpm_nhom15_be.promotions.application.pricing.PromotionConfigUtils;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.Promotion;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PromotionCustomerEligibility {
    public static final String TARGET_CUSTOMER_IDS_KEY = "targetCustomerIds";

    private static final List<String> TARGET_CUSTOMER_ID_KEYS = List.of(
        TARGET_CUSTOMER_IDS_KEY,
        "customerIds",
        "targetUserIds"
    );

    private PromotionCustomerEligibility() {
    }

    public static boolean isEligibleForUser(Promotion promotion, String userId) {
        if (promotion == null || userId == null || userId.isBlank() || userId.startsWith("guest-")) {
            return false;
        }

        return targetCustomerIds(promotion.getConfig()).contains(userId.trim());
    }

    public static Set<String> targetCustomerIds(Map<String, Object> config) {
        if (config == null || config.isEmpty()) {
            return Set.of();
        }

        Set<String> customerIds = new LinkedHashSet<>();
        for (String key : TARGET_CUSTOMER_ID_KEYS) {
            customerIds.addAll(PromotionConfigUtils.getStringList(config, key));
        }
        return customerIds;
    }
}
