package fit.iuh.kttkpm_nhom15_be.orders.presentation.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.kttkpm_nhom15_be.orders.application.usecases.CancelOrderUseCase;
import fit.iuh.kttkpm_nhom15_be.orders.application.usecases.GetMyOrderDetailUseCase;
import fit.iuh.kttkpm_nhom15_be.orders.application.usecases.GetMyOrderHistoryUseCase;
import fit.iuh.kttkpm_nhom15_be.orders.application.usecases.PlaceOrderUseCase;
import fit.iuh.kttkpm_nhom15_be.orders.application.usecases.QuoteShippingFeeUseCase;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingProvider;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.security.ShopperAccessGuard;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private QuoteShippingFeeUseCase quoteShippingFeeUseCase;
    @MockBean
    private GetMyOrderDetailUseCase getMyOrderDetailUseCase;
    @MockBean
    private GetMyOrderHistoryUseCase getMyOrderHistoryUseCase;
    @MockBean
    private ShopperAccessGuard shopperAccessGuard;

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
        when(shopperAccessGuard.resolveAllowedUserId(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @WithMockUser(username = "user-1")
    void getMyOrderDetailReturnsStructuredSuccessEnvelope() throws Exception {
        when(getMyOrderDetailUseCase.execute("user-1", "order-1")).thenReturn(
            new fit.iuh.kttkpm_nhom15_be.orders.application.dto.OrderDetailDTO(
                "order-1",
                "ORD-1",
                OrderStatus.COMPLETED,
                fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod.COD,
                PaymentStatus.UNPAID,
                BigDecimal.valueOf(200000),
                BigDecimal.ZERO,
                BigDecimal.valueOf(15000),
                BigDecimal.valueOf(215000),
                "Nguyen Van A",
                "0901234567",
                "123 Lê Lợi",
                "TP. Ho Chi Minh",
                "Quan 1",
                "Ben Nghe",
                LocalDateTime.of(2026, 5, 28, 10, 0),
                List.of()
            )
        );

        mockMvc.perform(get("/api/v1/orders/order-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Lấy chi tiết đơn hàng thành công"))
            .andExpect(jsonPath("$.data.orderId").value("order-1"))
            .andExpect(jsonPath("$.data.orderNo").value("ORD-1"))
            .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(username = "user-1")
    void getMyOrderHistoryReturnsStructuredSuccessEnvelope() throws Exception {
        when(getMyOrderHistoryUseCase.execute("user-1")).thenReturn(List.of(
            new fit.iuh.kttkpm_nhom15_be.orders.application.dto.OrderHistoryItemDTO(
                "order-1",
                "ORD-1",
                fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod.COD,
                PaymentStatus.UNPAID,
                OrderStatus.CREATED,
                "Nguyen Van A",
                BigDecimal.valueOf(250000),
                LocalDateTime.of(2026, 5, 28, 9, 30)
            )
        ));

        mockMvc.perform(get("/api/v1/orders/history"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Lấy lịch sử đơn hàng thành công"))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data[0].orderId").value("order-1"))
            .andExpect(jsonPath("$.data[0].orderNo").value("ORD-1"))
            .andExpect(jsonPath("$.data[0].paymentMethod").value("COD"));
    }

    @Test
    void quoteShippingFeeAllowsGuestAndReturnsStructuredSuccessEnvelope() throws Exception {
        when(quoteShippingFeeUseCase.execute(any())).thenReturn(
            new fit.iuh.kttkpm_nhom15_be.orders.application.dto.ShippingFeeQuoteDTO(
                ShippingProvider.GHTK,
                java.math.BigDecimal.valueOf(22000),
                java.math.BigDecimal.ZERO,
                true,
                1000,
                "Đã tính phí vận chuyển qua GHTK thành công."
            )
        );

        mockMvc.perform(post("/api/v1/orders/shipping-fee/quote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "shippingProvider", "GHTK",
                        "shipCity", "Ho Chi Minh",
                        "shipDistrict", "District 1",
                        "shipWard", "Ben Nghe",
                        "orderValue", 200000,
                        "itemQuantity", 2
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Tính phí vận chuyển thành công"))
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.data.shippingProvider").value("GHTK"))
            .andExpect(jsonPath("$.data.fee").value(22000))
            .andExpect(jsonPath("$.data.deliverySupported").value(true));
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
                .content(objectMapper.writeValueAsString(Map.ofEntries(
                        Map.entry("userId", "user-1"),
                        Map.entry("shipFullName", "Nguyen Van A"),
                        Map.entry("shipPhone", "0901234567"),
                        Map.entry("shipEmail", "customer@example.com"),
                        Map.entry("shipAddress", "123 Lê Lợi"),
                        Map.entry("shipCity", "Ho Chi Minh"),
                        Map.entry("shipDistrict", "District 1"),
                        Map.entry("shipWard", "Ben Nghe"),
                        Map.entry("shippingMode", "STANDARD"),
                        Map.entry("shippingFee", 30000),
                        Map.entry("paymentMethod", "COD")
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Đặt hàng thành công"))
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
                .content(objectMapper.writeValueAsString(Map.of(
                    "userId", "user-1",
                    "reason", "Khách hàng muốn hủy"
                ))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
            .andExpect(jsonPath("$.path").value("/api/v1/orders/order-404/cancel"));
    }
}
