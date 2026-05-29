package fit.iuh.kttkpm_nhom15_be.promotions.application.interfaces;

import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.AppliedPromotionDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartDTO;

import java.util.List;

public interface PromotionFacade {
    AppliedPromotionDTO validateAndCalculate(String code, OrderCartDTO cart);
    default AppliedPromotionDTO validateOrderDiscountAndCalculate(String code, OrderCartDTO cart) {
        return validateOrderDiscountAndCalculate(code, cart, null);
    }
    AppliedPromotionDTO validateOrderDiscountAndCalculate(String code, OrderCartDTO cart, String userId);
    AppliedPromotionDTO findBestAutomaticProductDiscount(OrderCartDTO cart);
    List<AppliedPromotionDTO> findAutomaticProductDiscounts(OrderCartDTO cart);
    void markPromotionUsed(String promotionId);
}
