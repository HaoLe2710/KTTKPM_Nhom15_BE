package fit.iuh.kttkpm_nhom15_be.orders.application.facades;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.reviews.application.interfaces.OrderFacade;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.OrderNotCompletedException;
import fit.iuh.kttkpm_nhom15_be.reviews.domain.exceptions.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * OrderFacadeForReviewsImpl - Provides order verification for Reviews module
 * 
 * Implements: fit.iuh.kttkpm_nhom15_be.reviews.application.interfaces.OrderFacade
 * Verification rules:
 * 1. Order must exist
 * 2. Order must belong to the user
 * 3. Order must be in COMPLETED status (ready for review)
 */
@Component
@RequiredArgsConstructor
public class OrderFacadeForReviewsImpl implements OrderFacade {

    private final OrderRepository orderRepository;

    @Override
    public void verifyOrderForReview(String orderId, String userId) {
        // 1. Find order by ID
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new OrderNotFoundException(orderId);
        }

        Order order = orderOpt.get();

        // 2. Verify order belongs to the user
        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException(orderId);  // Unauthorized access masked as not found
        }

        // 3. Verify order is in COMPLETED status
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new OrderNotCompletedException(orderId);
        }
    }
}
