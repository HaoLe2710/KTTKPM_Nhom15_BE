package fit.iuh.kttkpm_nhom15_be.reviews.application.interfaces;

public interface OrderFacade {
    /**
     * Verify that:
     * 1. Order exists
     * 2. Order belongs to the user
     * 3. Order is in a reviewable state (CREATED or COMPLETED)
     * 4. Product exists inside the order
     * 
     * @param orderId the order ID
     * @param userId the user ID
     * @param productId the product ID
     * @throws fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.OrderNotFoundException if order not found
     * @throws fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.OrderNotCompletedException if not reviewable
     */
    void verifyOrderForReview(String orderId, String userId, String productId);
}
