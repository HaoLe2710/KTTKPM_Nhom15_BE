package fit.iuh.kttkpm_nhom15_be.analytics.application.usecases;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import fit.iuh.kttkpm_nhom15_be.analytics.application.dto.DashboardReportResponse;
import fit.iuh.kttkpm_nhom15_be.analytics.application.dto.DashboardReportResponse.DailyMetric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportPdfUseCase {

    private final GenerateReportUseCase generateReportUseCase;

    public byte[] execute(LocalDateTime startDate, LocalDateTime endDate) {
        DashboardReportResponse reportData = generateReportUseCase.execute(startDate, endDate);
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, java.awt.Color.BLACK);
            Paragraph title = new Paragraph("Financial Analytics Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Date Range
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            Paragraph dateRange = new Paragraph("Period: " + startDate.format(formatter) + " to " + endDate.format(formatter));
            dateRange.setSpacingAfter(15);
            document.add(dateRange);

            // Summary Metrics
            Font metricFont = FontFactory.getFont(FontFactory.HELVETICA, 12, java.awt.Color.DARK_GRAY);
            document.add(new Paragraph("Net Revenue (Completed Orders): $" + reportData.getNetRevenue(), metricFont));
            document.add(new Paragraph("Order Success Rate: " + reportData.getSuccessRate() + "%", metricFont));
            document.add(new Paragraph(" ")); // blank line

            // Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2f, 2f, 3f});

            // Table Headers
            addTableHeader(table, "Date");
            addTableHeader(table, "Total Orders");
            addTableHeader(table, "Completed Orders");
            addTableHeader(table, "Revenue");

            // Table Rows
            for (DailyMetric metric : reportData.getChartData()) {
                table.addCell(new PdfPCell(new Phrase(metric.getDate())));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(metric.getTotalOrders()))));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(metric.getCompletedOrders()))));
                table.addCell(new PdfPCell(new Phrase("$" + metric.getRevenue().toString())));
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            log.error("Failed to generate PDF report", e);
            throw new RuntimeException("Error generating PDF report", e);
        }
    }

    private void addTableHeader(PdfPTable table, String headerTitle) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        header.setBorderWidth(1);
        header.setPhrase(new Phrase(headerTitle));
        table.addCell(header);
    }
}
