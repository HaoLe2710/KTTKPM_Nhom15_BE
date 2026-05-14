package fit.iuh.kttkpm_nhom15_be.analytics.application.usecases;

import fit.iuh.kttkpm_nhom15_be.analytics.application.dto.DashboardReportResponse;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.DailyOrderStat;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.RawOrderStatsDTO;
import fit.iuh.kttkpm_nhom15_be.orders.application.interfaces.OrderFacade;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnalyticsUseCaseTest {

    @Test
    void generateReportAggregatesCompletedRevenuePerDayAndRoundsSuccessRate() {
        OrderFacade orderFacade = Mockito.mock(OrderFacade.class);
        GenerateReportUseCase useCase = new GenerateReportUseCase(orderFacade);
        LocalDateTime startDate = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 3, 2, 23, 59);

        when(orderFacade.getOrderStatistics(startDate, endDate)).thenReturn(RawOrderStatsDTO.builder()
            .dailyStats(List.of(
                DailyOrderStat.builder()
                    .statDate(LocalDate.of(2026, 3, 2))
                    .status("COMPLETED")
                    .orderCount(1)
                    .revenue(new BigDecimal("49.75"))
                    .build(),
                DailyOrderStat.builder()
                    .statDate(LocalDate.of(2026, 3, 1))
                    .status("COMPLETED")
                    .orderCount(1)
                    .revenue(new BigDecimal("100.25"))
                    .build(),
                DailyOrderStat.builder()
                    .statDate(LocalDate.of(2026, 3, 1))
                    .status("CANCELLED")
                    .orderCount(1)
                    .revenue(new BigDecimal("500.00"))
                    .build()
            ))
            .build());

        DashboardReportResponse result = useCase.execute(startDate, endDate);

        assertEquals(new BigDecimal("150.00"), result.getNetRevenue());
        assertEquals(66.67, result.getSuccessRate());
        assertEquals(2, result.getChartData().size());
        assertEquals("2026-03-01", result.getChartData().get(0).getDate());
        assertEquals(2, result.getChartData().get(0).getTotalOrders());
        assertEquals(1, result.getChartData().get(0).getCompletedOrders());
        assertEquals(new BigDecimal("100.25"), result.getChartData().get(0).getRevenue());
        assertEquals("2026-03-02", result.getChartData().get(1).getDate());
    }

    @Test
    void exportPdfReturnsPdfDocumentBytes() {
        GenerateReportUseCase generateReportUseCase = Mockito.mock(GenerateReportUseCase.class);
        ExportPdfUseCase useCase = new ExportPdfUseCase(generateReportUseCase);
        LocalDateTime startDate = LocalDateTime.of(2026, 3, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 3, 2, 23, 59);

        when(generateReportUseCase.execute(startDate, endDate)).thenReturn(DashboardReportResponse.builder()
            .netRevenue(new BigDecimal("150.00"))
            .successRate(66.67)
            .chartData(List.of(
                DashboardReportResponse.DailyMetric.builder()
                    .date("2026-03-01")
                    .totalOrders(2)
                    .completedOrders(1)
                    .revenue(new BigDecimal("100.25"))
                    .build()
            ))
            .build());

        byte[] pdf = useCase.execute(startDate, endDate);

        assertTrue(pdf.length > 0);
        assertTrue(new String(pdf, StandardCharsets.ISO_8859_1).startsWith("%PDF"));
        verify(generateReportUseCase).execute(startDate, endDate);
    }
}
