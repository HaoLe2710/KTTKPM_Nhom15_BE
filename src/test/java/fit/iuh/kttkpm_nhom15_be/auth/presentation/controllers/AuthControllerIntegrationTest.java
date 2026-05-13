package fit.iuh.kttkpm_nhom15_be.auth.presentation.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.kttkpm_nhom15_be.auth.application.services.OtpService;
import fit.iuh.kttkpm_nhom15_be.auth.application.usecases.LoginUseCase;
import fit.iuh.kttkpm_nhom15_be.auth.application.usecases.RegisterUseCase;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.JwtProvider;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.OAuth2SuccessHandler;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.SecurityConfig;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private LoginUseCase loginUseCase;
  @MockBean
  private RegisterUseCase registerUseCase;
  @MockBean
  private OtpService otpService;
  @MockBean
  private UserFacade userFacade;
  @MockBean
  private JwtProvider jwtProvider;
  @MockBean
  private OAuth2SuccessHandler oAuth2SuccessHandler;
  @MockBean
  private fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.JwtAuthenticationFilter jwtAuthenticationFilter;

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
  void loginReturnsAccessTokenAndRoleAndUserIdWhenEmailProvided() throws Exception {
    when(loginUseCase.execute("admin.local@kttkpm.dev", "Admin@123")).thenReturn("token-123");
    when(jwtProvider.extractUserId("token-123")).thenReturn("seed-admin-0001");
    when(jwtProvider.extractRole("token-123")).thenReturn("ADMIN");

    mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(Map.of(
          "email", "admin.local@kttkpm.dev",
          "password", "Admin@123"
        ))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.accessToken").value("token-123"))
      .andExpect(jsonPath("$.tokenType").value("Bearer"))
      .andExpect(jsonPath("$.userId").value("seed-admin-0001"))
      .andExpect(jsonPath("$.role").value("ADMIN"));
  }

  @Test
  void loginTrimsIdentifierAndAcceptsLegacyIdentifierField() throws Exception {
    when(loginUseCase.execute(anyString(), anyString())).thenReturn("token-legacy");
    when(jwtProvider.extractUserId("token-legacy")).thenReturn("u-1");
    when(jwtProvider.extractRole("token-legacy")).thenReturn("STAFF");

    mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(Map.of(
          "identifier", " admin.local@kttkpm.dev ",
          "password", "Admin@123"
        ))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.accessToken").value("token-legacy"))
      .andExpect(jsonPath("$.role").value("STAFF"));
  }
}
