package fit.iuh.kttkpm_nhom15_be.orders.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawOrderStatsDTO {
  private List<DailyOrderStat> dailyStats;
}
