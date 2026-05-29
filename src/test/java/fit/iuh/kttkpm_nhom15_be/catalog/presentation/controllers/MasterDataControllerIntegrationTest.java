package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.OptionRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.OptionResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.OptionValueResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.ProductTypeRequest;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin.MasterDataDTOs.ProductTypeResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.services.CatalogAdminService;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.CreateOptionUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.CreateProductTypeUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.DeleteOptionUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.DeleteProductTypeUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.GetOptionsUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.GetProductTypesUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.UpdateOptionUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.usecases.admin.masterdata.UpdateProductTypeUseCase;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.JwtAuthenticationFilter;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.OAuth2SuccessHandler;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.SecurityConfig;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MasterDataController.class)
@Import(SecurityConfig.class)
class MasterDataControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateProductTypeUseCase createProductTypeUseCase;
    @MockBean
    private UpdateProductTypeUseCase updateProductTypeUseCase;
    @MockBean
    private DeleteProductTypeUseCase deleteProductTypeUseCase;
    @MockBean
    private GetProductTypesUseCase getProductTypesUseCase;
    @MockBean
    private CreateOptionUseCase createOptionUseCase;
    @MockBean
    private UpdateOptionUseCase updateOptionUseCase;
    @MockBean
    private DeleteOptionUseCase deleteOptionUseCase;
    @MockBean
    private GetOptionsUseCase getOptionsUseCase;
    @MockBean
    private CatalogAdminService catalogAdminService;

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
    @WithMockUser(roles = "ADMIN")
    void updateProductTypeReturns200ForAdmin() throws Exception {
        ProductTypeRequest request = ProductTypeRequest.builder()
                .code("SKINCARE")
                .name("Skincare")
                .build();
        ProductTypeResponse response = ProductTypeResponse.builder()
                .id("type-1")
                .code("SKINCARE")
                .name("Skincare")
                .build();
        when(createProductTypeUseCase.execute(any(ProductTypeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/product-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("type-1"))
                .andExpect(jsonPath("$.data.code").value("SKINCARE"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void updateProductTypeReturns403ForNonAdmin() throws Exception {
        ProductTypeRequest request = ProductTypeRequest.builder()
                .code("SKINCARE")
                .name("Skincare")
                .build();

        mockMvc.perform(post("/api/v1/product-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProductTypeReturns200ForAdmin() throws Exception {
        doNothing().when(deleteProductTypeUseCase).execute("type-1");

        mockMvc.perform(delete("/api/v1/product-types/type-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Loại sản phẩm đã được xóa thành công"));

        verify(deleteProductTypeUseCase).execute("type-1");
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void deleteProductTypeReturns403ForNonAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/product-types/type-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateOptionReturns200ForAdmin() throws Exception {
        OptionRequest request = OptionRequest.builder()
                .code("SIZE")
                .name("Size")
                .values(List.of("S", "M"))
                .build();
        OptionResponse response = OptionResponse.builder()
                .id("opt-1")
                .code("SIZE")
                .name("Size")
                .values(List.of(
                        OptionValueResponse.builder().id("val-1").value("S").build(),
                        OptionValueResponse.builder().id("val-2").value("M").build()
                ))
                .build();
        when(updateOptionUseCase.execute(eq("opt-1"), any(OptionRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/options/opt-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("opt-1"))
                .andExpect(jsonPath("$.data.values.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void updateOptionReturns403ForNonAdmin() throws Exception {
        OptionRequest request = OptionRequest.builder()
                .code("SIZE")
                .name("Size")
                .values(List.of("S"))
                .build();

        mockMvc.perform(put("/api/v1/options/opt-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteOptionReturns200ForAdmin() throws Exception {
        doNothing().when(deleteOptionUseCase).execute("opt-1");

        mockMvc.perform(delete("/api/v1/options/opt-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Tùy chọn đã được xóa thành công"));

        verify(deleteOptionUseCase).execute("opt-1");
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void deleteOptionReturns403ForNonAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/options/opt-1"))
                .andExpect(status().isForbidden());
    }
}
