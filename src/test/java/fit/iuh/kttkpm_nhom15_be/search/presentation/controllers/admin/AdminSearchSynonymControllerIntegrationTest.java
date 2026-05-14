package fit.iuh.kttkpm_nhom15_be.search.presentation.controllers.admin;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.admin.SearchAdminDtos.SynonymRecommendationResponse;
import fit.iuh.kttkpm_nhom15_be.search.application.services.SearchAdminService;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.admin.AdminApiRoleInterceptor;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.admin.AdminWebMvcConfig;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.JwtAuthenticationFilter;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.OAuth2SuccessHandler;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.SecurityConfig;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.support.AdminPageRequestFactory;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminSearchSynonymController.class)
@Import({SecurityConfig.class, AdminWebMvcConfig.class, AdminApiRoleInterceptor.class})
class AdminSearchSynonymControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private SearchAdminService searchAdminService;
  @MockBean
  private AdminPageRequestFactory adminPageRequestFactory;
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
  void recommendSynonymsReturnsRecommendationsForAdminRequest() throws Exception {
    when(searchAdminService.recommendSynonyms(eq("vi"), eq(5))).thenReturn(List.of(
      new SynonymRecommendationResponse("kem chống nắng", "vi", "syn-kem-chong-nang", 90)
    ));

    mockMvc.perform(post("/api/v1/admin/search/synonyms/recommendations")
        .header("X-User-Role", "ADMIN")
        .header("X-User-Id", "seed-admin-0001")
        .param("locale", "vi")
        .param("limit", "5"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].keyword").value("kem chống nắng"))
      .andExpect(jsonPath("$[0].suggestedCode").value("syn-kem-chong-nang"))
      .andExpect(jsonPath("$[0].confidence").value(90));
  }
}
