package fit.iuh.kttkpm_nhom15_be.orders.infrastructure.persistence.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fit.iuh.kttkpm_nhom15_be.orders.application.dto.DailyOrderStat;
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
}
