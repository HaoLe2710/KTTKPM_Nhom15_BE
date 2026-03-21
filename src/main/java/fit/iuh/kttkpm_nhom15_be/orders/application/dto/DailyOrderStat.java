package fit.iuh.kttkpm_nhom15_be.orders.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyOrderStat {
  private LocalDate statDate;
  private String status;
  private long orderCount;
  private BigDecimal revenue;
}
