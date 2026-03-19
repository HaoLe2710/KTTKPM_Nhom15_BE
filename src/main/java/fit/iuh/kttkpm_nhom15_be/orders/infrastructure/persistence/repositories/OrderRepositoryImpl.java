package fit.iuh.kttkpm_nhom15_be.orders.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.orders.infrastructure.persistence.entities.OrderJpaEntity;
import fit.iuh.kttkpm_nhom15_be.orders.infrastructure.persistence.mappers.OrderDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// 1. Interface Spring Data JPA thông thường
interface JpaOrderRepository extends JpaRepository<OrderJpaEntity, String> {
}

// 2. Lớp Implement interface của tầng Domain
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;
    private final OrderDataMapper orderDataMapper;

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = orderDataMapper.toJpaEntity(order);
        OrderJpaEntity savedEntity = jpaOrderRepository.save(entity);
        return orderDataMapper.toDomainModel(savedEntity);
    }

    @Override
    public Optional<Order> findById(String id) {
        return jpaOrderRepository.findById(id)
                .map(orderDataMapper::toDomainModel);
    }
}