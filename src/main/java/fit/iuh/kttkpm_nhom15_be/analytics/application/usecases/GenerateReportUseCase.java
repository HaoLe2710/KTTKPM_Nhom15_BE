package fit.iuh.kttkpm_nhom15_be.analytics.application.usecases;

import fit.iuh.kttkpm_nhom15_be.analytics.application.dto.DashboardReportResponse;
import fit.iuh.kttkpm_nhom15_be.analytics.application.dto.DashboardReportResponse.DailyMetric;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.DailyOrderStat;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.RawOrderStatsDTO;
import fit.iuh.kttkpm_nhom15_be.orders.application.interfaces.OrderFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GenerateReportUseCase {

    private final OrderFacade orderFacade;

    public DashboardReportResponse execute(LocalDateTime startDate, LocalDateTime endDate) {
        RawOrderStatsDTO rawData = orderFacade.getOrderStatistics(startDate, endDate);
        long periodSeconds = Math.max(1L, Duration.between(startDate, endDate).getSeconds() + 1L);
        RawOrderStatsDTO previousData = orderFacade.getOrderStatistics(
                startDate.minusSeconds(periodSeconds),
                startDate.minusSeconds(1)
        );

        BigDecimal netRevenue = BigDecimal.ZERO;
        long totalOrders = 0;
        long completedOrders = 0;

        // Group by Date for charting
        Map<LocalDate, DailyMetric> dailyMetricsMap = new TreeMap<>(); // Sorted by date

        for (DailyOrderStat stat : rawData.getDailyStats()) {
            LocalDate date = stat.getStatDate();
            dailyMetricsMap.putIfAbsent(date, DailyMetric.builder()
                .date(date.toString())
                .totalOrders(0)
                .revenue(BigDecimal.ZERO)
                .completedOrders(0)
                .build());

            DailyMetric metric = dailyMetricsMap.get(date);
            metric.setTotalOrders(metric.getTotalOrders() + stat.getOrderCount());
            totalOrders += stat.getOrderCount();

            if ("COMPLETED".equalsIgnoreCase(stat.getStatus())) {
                metric.setRevenue(metric.getRevenue().add(stat.getRevenue()));
                metric.setCompletedOrders(metric.getCompletedOrders() + stat.getOrderCount());

                netRevenue = netRevenue.add(stat.getRevenue());
                completedOrders += stat.getOrderCount();
            }
        }

        BigDecimal previousNetRevenue = computeCompletedRevenue(previousData);
        BigDecimal deltaPct = computeDeltaPct(previousNetRevenue, netRevenue);
        String statusLabel = resolveStatusLabel(deltaPct);

        double successRate = totalOrders == 0 ? 0.0 : ((double) completedOrders / totalOrders);
        successRate = BigDecimal.valueOf(successRate).setScale(4, RoundingMode.HALF_UP).doubleValue();

        List<DailyMetric> chartData = new ArrayList<>(dailyMetricsMap.values());
        if (!chartData.isEmpty()) {
            chartData.get(chartData.size() - 1).setIsAccent(Boolean.TRUE);
        }

        return DashboardReportResponse.builder()
            .netRevenue(netRevenue)
            .successRate(successRate)
            .deltaPct(deltaPct)
            .statusLabel(statusLabel)
            .chartData(chartData)
            .build();
    }

    private BigDecimal computeCompletedRevenue(RawOrderStatsDTO stats) {
        BigDecimal revenue = BigDecimal.ZERO;
        for (DailyOrderStat stat : stats.getDailyStats()) {
            if ("COMPLETED".equalsIgnoreCase(stat.getStatus())) {
                revenue = revenue.add(stat.getRevenue());
            }
        }
        return revenue;
    }

    private BigDecimal computeDeltaPct(BigDecimal baseline, BigDecimal current) {
        if (baseline.compareTo(BigDecimal.ZERO) == 0) {
            if (current.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return BigDecimal.valueOf(100);
        }
        return current.subtract(baseline)
                .multiply(BigDecimal.valueOf(100))
                .divide(baseline, 2, RoundingMode.HALF_UP);
    }

    private String resolveStatusLabel(BigDecimal deltaPct) {
        int sign = deltaPct.compareTo(BigDecimal.ZERO);
        if (sign > 0) {
            return "UP";
        }
        if (sign < 0) {
            return "DOWN";
        }
        return "FLAT";
    }
}
