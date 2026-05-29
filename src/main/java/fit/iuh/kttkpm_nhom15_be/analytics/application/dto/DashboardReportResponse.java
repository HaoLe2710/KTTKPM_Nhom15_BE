package fit.iuh.kttkpm_nhom15_be.analytics.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardReportResponse {
    private BigDecimal netRevenue;
    private double successRate;
    private BigDecimal deltaPct;
    private String statusLabel;
    private List<DailyMetric> chartData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyMetric {
        private String date;
        private BigDecimal revenue;
        private Boolean isAccent;
        private long totalOrders;
        private long completedOrders;
    }
}
