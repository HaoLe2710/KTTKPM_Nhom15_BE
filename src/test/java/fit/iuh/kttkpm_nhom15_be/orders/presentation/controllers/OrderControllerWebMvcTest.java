package fit.iuh.kttkpm_nhom15_be.orders.presentation.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.kttkpm_nhom15_be.orders.application.usecases.CancelOrderUseCase;
import fit.iuh.kttkpm_nhom15_be.orders.application.usecases.PlaceOrderUseCase;
import fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions.OrderNotFoundException;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.JwtAuthenticationFilter;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.OAuth2SuccessHandler;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.SecurityConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PlaceOrderUseCase placeOrderUseCase;
    @MockBean
    private CancelOrderUseCase cancelOrderUseCase;

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
    void placeOrderReturnsStructuredSuccessEnvelope() throws Exception {
        when(placeOrderUseCase.execute(any())).thenReturn(
            fit.iuh.kttkpm_nhom15_be.orders.application.results.PlaceOrderResult.builder()
                .orderId("order-123")
                .orderNo("ORD-123")
                .paymentRedirectUrl("https://payment.example/redirect")
                .build()
        );

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "userId", "user-1",
                        "shipFullName", "Nguyen Van A",
                        "shipPhone", "0901234567",
                        "shipAddress", "123 Le Loi",
                        "shipCity", "Ho Chi Minh",
                        "shipDistrict", "District 1",
                        "shipWard", "Ben Nghe",
                        "shippingMode", "STANDARD",
                        "shippingFee", 30000,
                        "paymentMethod", "COD"
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Dat hang thanh cong"))
            .andExpect(jsonPath("$.status").value(201))
            .andExpect(jsonPath("$.data.orderId").value("order-123"))
            .andExpect(jsonPath("$.data.orderNo").value("ORD-123"));
    }

    @Test
    @WithMockUser
    void cancelOrderReturnsStructuredNotFoundError() throws Exception {
        when(cancelOrderUseCase.execute(any()))
            .thenThrow(new OrderNotFoundException("order-404"));

        mockMvc.perform(post("/api/v1/orders/order-404/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("reason", "Khach hang muon huy"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
            .andExpect(jsonPath("$.path").value("/api/v1/orders/order-404/cancel"));
    }
}
