package fit.iuh.kttkpm_nhom15_be.orders.infrastructure.persistence.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fit.iuh.kttkpm_nhom15_be.orders.application.dto.DailyOrderStat;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.OrderHistoryItemDTO;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.RawOrderStatsDTO;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.orders.infrastructure.persistence.entities.OrderJpaEntity;
import fit.iuh.kttkpm_nhom15_be.orders.infrastructure.persistence.mappers.OrderDataMapper;
import lombok.RequiredArgsConstructor;

// 1. Interface Spring Data JPA thông thường
interface JpaOrderRepository extends JpaRepository<OrderJpaEntity, String> {

    @Query(value = "SELECT CAST(created_at AS DATE) as statDate, status as status, COUNT(id) as orderCount, SUM(total_amount) as revenue " +
                   "FROM orders " +
                   "WHERE created_at >= :startDate AND created_at <= :endDate " +
                   "GROUP BY CAST(created_at AS DATE), status " +
                   "ORDER BY statDate ASC", 
           nativeQuery = true)
    List<Object[]> getOrderStatistics(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    boolean existsByPromotionId(String promotionId);
    boolean existsByUserId(String userId);

    @Query("""
        SELECT DISTINCT o
        FROM OrderJpaEntity o
        JOIN o.items i
        WHERE o.userId = :userId
          AND o.status IN (
            fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus.CREATED,
            fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus.COMPLETED
          )
          AND i.productId = :productId
        ORDER BY o.updatedAt DESC, o.createdAt DESC
        """)
    List<OrderJpaEntity> findReviewableOrdersByUserIdAndProductId(
        @Param("userId") String userId,
        @Param("productId") String productId,
        org.springframework.data.domain.Pageable pageable
    );

    List<OrderJpaEntity> findByUserIdOrderByCreatedAtDesc(String userId);

    @EntityGraph(attributePaths = "items")
    Optional<OrderJpaEntity> findByIdAndUserId(String id, String userId);
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

    @Override
    public Optional<Order> findByIdAndUserId(String id, String userId) {
        return jpaOrderRepository.findByIdAndUserId(id, userId)
            .map(orderDataMapper::toDomainModel);
    }

    @Override
    public Optional<Order> findLatestReviewableOrderByUserIdAndProductId(String userId, String productId) {
        return jpaOrderRepository.findReviewableOrdersByUserIdAndProductId(userId, productId, PageRequest.of(0, 1))
            .stream()
            .findFirst()
            .map(orderDataMapper::toDomainModel);
    }

    @Override
    public List<OrderHistoryItemDTO> findOrderHistoryByUserId(String userId) {
        return jpaOrderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(entity -> new OrderHistoryItemDTO(
                entity.getId(),
                entity.getOrderNo(),
                entity.getPaymentMethod(),
                entity.getPaymentStatus(),
                entity.getStatus(),
                entity.getShipFullName(),
                entity.getTotalAmount(),
                entity.getCreatedAt()
            ))
            .toList();
    }

    @Override
    public RawOrderStatsDTO getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> rawStats = jpaOrderRepository.getOrderStatistics(startDate, endDate);
        List<DailyOrderStat> dailyStats = rawStats.stream().map(row -> {
            return DailyOrderStat.builder()
                .statDate(
                    ((java.time.temporal.TemporalAccessor) row[0]) instanceof LocalDate
                        ? (LocalDate) row[0]
                        : ((java.sql.Date) row[0]).toLocalDate()
                )
                .status((String) row[1])
                .orderCount(((Number) row[2]).longValue())
                .revenue((BigDecimal) row[3])
                .build();
        }).toList();
        return RawOrderStatsDTO.builder().dailyStats(dailyStats).build();
    }

    @Override
    public boolean existsByPromotionId(String promotionId) {
        return jpaOrderRepository.existsByPromotionId(promotionId);
    }
  
    @Override
    public boolean hasOrdersByUser(String userId) {
        return jpaOrderRepository.existsByUserId(userId);
    }
}
