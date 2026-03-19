package fit.iuh.kttkpm_nhom15_be.orders.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(String id);
}