package fit.iuh.kttkpm_nhom15_be.reviews.application.interfaces;

public interface OrderFacade {
    /**
     * Verify that:
     * 1. Order exists
     * 2. Order belongs to the user
     * 3. Order is in COMPLETED state (ready for review)
     * 
     * @param orderId the order ID
     * @param userId the user ID
     * @throws fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.OrderNotFoundException if order not found
     * @throws fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.OrderNotCompletedException if not completed
     */
    void verifyOrderForReview(String orderId, String userId);
}
