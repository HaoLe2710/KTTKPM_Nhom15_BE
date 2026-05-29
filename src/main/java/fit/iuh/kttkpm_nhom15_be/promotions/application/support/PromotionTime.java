package fit.iuh.kttkpm_nhom15_be.promotions.application.support;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class PromotionTime {
    private static final ZoneId PROMOTION_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private PromotionTime() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(PROMOTION_ZONE);
    }
}
