package fit.iuh.kttkpm_nhom15_be.promotions.application.interfaces;

import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.AppliedPromotionDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartDTO;

public interface PromotionFacade {
    AppliedPromotionDTO validateAndCalculate(String code, OrderCartDTO cart);
    void markPromotionUsed(String promotionId);
}
