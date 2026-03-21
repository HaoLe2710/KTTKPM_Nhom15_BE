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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenerateReportUseCase {

    private final OrderFacade orderFacade;

    public DashboardReportResponse execute(LocalDateTime startDate, LocalDateTime endDate) {
        RawOrderStatsDTO rawData = orderFacade.getOrderStatistics(startDate, endDate);

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

        double successRate = totalOrders == 0 ? 0.0 : ((double) completedOrders / totalOrders) * 100.0;
        // Round to 2 decimal places
        successRate = BigDecimal.valueOf(successRate).setScale(2, RoundingMode.HALF_UP).doubleValue();

        return DashboardReportResponse.builder()
            .netRevenue(netRevenue)
            .successRate(successRate)
            .chartData(new ArrayList<>(dailyMetricsMap.values()))
            .build();
    }
}
