package fit.iuh.kttkpm_nhom15_be.orders.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.OrderHistoryItemDTO;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.RawOrderStatsDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(String id);
    Optional<Order> findByIdAndUserId(String id, String userId);
    Optional<Order> findByIdAndUserIdForUpdate(String id, String userId);
    Optional<Order> findLatestReviewableOrderByUserIdAndProductId(String userId, String productId);
    List<OrderHistoryItemDTO> findOrderHistoryByUserId(String userId);
    RawOrderStatsDTO getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate);
    boolean existsByPromotionId(String promotionId);
    boolean hasOrdersByUser(String userId);
}
