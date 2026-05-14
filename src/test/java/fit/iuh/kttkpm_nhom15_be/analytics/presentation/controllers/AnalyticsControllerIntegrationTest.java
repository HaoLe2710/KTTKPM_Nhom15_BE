package fit.iuh.kttkpm_nhom15_be.analytics.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.analytics.application.dto.DashboardReportResponse;
import fit.iuh.kttkpm_nhom15_be.analytics.application.usecases.ExportPdfUseCase;
import fit.iuh.kttkpm_nhom15_be.analytics.application.usecases.GenerateReportUseCase;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.JwtAuthenticationFilter;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.OAuth2SuccessHandler;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.SecurityConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalyticsController.class)
@Import(SecurityConfig.class)
class AnalyticsControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private GenerateReportUseCase generateReportUseCase;
  @MockBean
  private ExportPdfUseCase exportPdfUseCase;
  @MockBean
  private OAuth2SuccessHandler oAuth2SuccessHandler;
  @MockBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @BeforeEach
  void setUpFilter() throws Exception {
    doAnswer(invocation -> {
      ServletRequest request = invocation.getArgument(0);
      ServletResponse response = invocation.getArgument(1);
      FilterChain chain = invocation.getArgument(2);
      chain.doFilter(request, response);
      return null;
    }).when(jwtAuthenticationFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
  }

  @Test
  @WithMockUser
  void getDashboardReturnsExtendedFieldsForAdminDashboard() throws Exception {
    DashboardReportResponse response = DashboardReportResponse.builder()
      .netRevenue(new BigDecimal("1250000.00"))
      .successRate(0.875)
      .deltaPct(new BigDecimal("12.50"))
      .statusLabel("UP")
      .chartData(List.of(
        DashboardReportResponse.DailyMetric.builder().date("2026-05-07").revenue(new BigDecimal("500000")).isAccent(false).build(),
        DashboardReportResponse.DailyMetric.builder().date("2026-05-08").revenue(new BigDecimal("750000")).isAccent(true).build()
      ))
      .build();
    when(generateReportUseCase.execute(any(), any())).thenReturn(response);

    mockMvc.perform(get("/api/v1/analytics/dashboard")
        .param("startDate", "2026-05-01T00:00:00")
        .param("endDate", "2026-05-08T23:59:59"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.netRevenue").value(1250000.00))
      .andExpect(jsonPath("$.successRate").value(0.875))
      .andExpect(jsonPath("$.deltaPct").value(12.50))
      .andExpect(jsonPath("$.statusLabel").value("UP"))
      .andExpect(jsonPath("$.chartData[1].isAccent").value(true));
  }
}
