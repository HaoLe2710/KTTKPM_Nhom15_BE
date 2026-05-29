package fit.iuh.kttkpm_nhom15_be.orders.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.orders.application.dto.RawOrderStatsDTO;
import fit.iuh.kttkpm_nhom15_be.orders.infrastructure.persistence.mappers.OrderDataMapper;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderRepositoryImplTest {

  @Test
  void getOrderStatisticsMapsSqlDateFromNativeQuery() {
    JpaOrderRepository jpaOrderRepository = mock(JpaOrderRepository.class);
    OrderRepositoryImpl repository = new OrderRepositoryImpl(jpaOrderRepository, mock(OrderDataMapper.class));
    LocalDateTime startDate = LocalDateTime.of(2026, 5, 1, 0, 0);
    LocalDateTime endDate = LocalDateTime.of(2026, 5, 29, 23, 59);

    when(jpaOrderRepository.getOrderStatistics(startDate, endDate)).thenReturn(List.<Object[]>of(
      new Object[] { Date.valueOf("2026-05-29"), "COMPLETED", 2L, new BigDecimal("120000.00") }
    ));

    RawOrderStatsDTO result = repository.getOrderStatistics(startDate, endDate);

    assertEquals(1, result.getDailyStats().size());
    assertEquals(LocalDate.of(2026, 5, 29), result.getDailyStats().getFirst().getStatDate());
    assertEquals("COMPLETED", result.getDailyStats().getFirst().getStatus());
    assertEquals(2L, result.getDailyStats().getFirst().getOrderCount());
    assertEquals(new BigDecimal("120000.00"), result.getDailyStats().getFirst().getRevenue());
  }
}
