package fit.iuh.kttkpm_nhom15_be.orders.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import java.util.Optional;

import fit.iuh.kttkpm_nhom15_be.orders.application.dto.RawOrderStatsDTO;
import java.time.LocalDateTime;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(String id);
    RawOrderStatsDTO getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate);

    boolean hasOrdersByUser(String userId);
}