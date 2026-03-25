package fit.iuh.kttkpm_nhom15_be.promotions.domain.exceptions;

public class PromotionNotFoundException extends RuntimeException {
    public PromotionNotFoundException(String promotionId) {
        super("Promotion not found: " + promotionId);
    }
}
