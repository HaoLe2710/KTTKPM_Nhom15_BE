package fit.iuh.kttkpm_nhom15_be.analytics.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.analytics.application.dto.DashboardReportResponse;
import fit.iuh.kttkpm_nhom15_be.analytics.application.usecases.ExportPdfUseCase;
import fit.iuh.kttkpm_nhom15_be.analytics.application.usecases.GenerateReportUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final GenerateReportUseCase generateReportUseCase;
    private final ExportPdfUseCase exportPdfUseCase;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardReportResponse> getDashboardReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        DashboardReportResponse response = generateReportUseCase.execute(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> downloadPdfReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        byte[] pdfBytes = exportPdfUseCase.execute(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "AnalyticsReport.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
