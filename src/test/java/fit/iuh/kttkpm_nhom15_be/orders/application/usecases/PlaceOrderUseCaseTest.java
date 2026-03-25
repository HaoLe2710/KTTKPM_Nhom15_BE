package fit.iuh.kttkpm_nhom15_be.orders.application.usecases;

import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartDTO;
import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartItemDTO;
import fit.iuh.kttkpm_nhom15_be.carts.application.interfaces.CartFacade;
import fit.iuh.kttkpm_nhom15_be.catalog.application.interfaces.CatalogFacade;
import fit.iuh.kttkpm_nhom15_be.orders.application.commands.PlaceOrderCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.VariantSnapshot;
import fit.iuh.kttkpm_nhom15_be.orders.application.results.PlaceOrderResult;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingMode;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.AppliedPromotionDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.interfaces.PromotionFacade;
import fit.iuh.kttkpm_nhom15_be.promotions.domain.exceptions.PromotionNotApplicableException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlaceOrderUseCaseTest {

    @Test
    void placeOrderWithoutPromotionKeepsPromotionFieldsNull() {
        TestFixture fixture = new TestFixture();
        when(fixture.promotionFacade.validateAndCalculate(any(), any())).thenThrow(new AssertionError("Should not be called"));

        PlaceOrderResult result = fixture.useCase.execute(fixture.command(null));

        ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
        verify(fixture.orderRepository).save(savedOrder.capture());
        assertEquals("order-1", result.getOrderId());
        assertNull(savedOrder.getValue().getPromotionId());
        assertEquals(BigDecimal.ZERO, savedOrder.getValue().getDiscountAmount());
        verify(fixture.promotionFacade, never()).markPromotionUsed(any());
    }

    @Test
    void placeOrderWithPromotionStoresDiscountAndMarksUsage() {
        TestFixture fixture = new TestFixture();
        when(fixture.promotionFacade.validateAndCalculate(any(), any()))
            .thenReturn(new AppliedPromotionDTO("promo-1", "SALE10", BigDecimal.TEN));

        fixture.useCase.execute(fixture.command("sale10"));

        ArgumentCaptor<Order> savedOrder = ArgumentCaptor.forClass(Order.class);
        verify(fixture.orderRepository).save(savedOrder.capture());
        assertEquals("promo-1", savedOrder.getValue().getPromotionId());
        assertEquals("SALE10", savedOrder.getValue().getPromotionCode());
        assertEquals(BigDecimal.TEN, savedOrder.getValue().getDiscountAmount());
        verify(fixture.promotionFacade).markPromotionUsed("promo-1");
    }

    @Test
    void placeOrderFailsWhenPromotionIsNotApplicable() {
        TestFixture fixture = new TestFixture();
        when(fixture.promotionFacade.validateAndCalculate(any(), any()))
            .thenThrow(new PromotionNotApplicableException("Invalid promotion"));

        assertThrows(PromotionNotApplicableException.class, () -> fixture.useCase.execute(fixture.command("bad-code")));
        verify(fixture.orderRepository, never()).save(any());
    }

    private static class TestFixture {
        private final OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        private final CartFacade cartFacade = Mockito.mock(CartFacade.class);
        private final CatalogFacade catalogFacade = Mockito.mock(CatalogFacade.class);
        private final PromotionFacade promotionFacade = Mockito.mock(PromotionFacade.class);
        private final ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        private final PlaceOrderUseCase useCase = new PlaceOrderUseCase(orderRepository, cartFacade, catalogFacade, promotionFacade, eventPublisher);

        private TestFixture() {
            CartDTO cart = CartDTO.builder()
                .cartId("cart-1")
                .userId("user-1")
                .items(List.of(CartItemDTO.builder()
                    .variantId("variant-1")
                    .quantity(2)
                    .price(BigDecimal.valueOf(100))
                    .build()))
                .build();
            VariantSnapshot snapshot = VariantSnapshot.builder()
                .variantId("variant-1")
                .sku("SKU-1")
                .productName("Sneaker")
                .imageUrl("img")
                .currentPrice(BigDecimal.valueOf(100))
                .attributes(Map.of("size", "42"))
                .build();

            when(cartFacade.getActiveCart("user-1")).thenReturn(cart);
            when(catalogFacade.validateAndGetSnapshots(cart.getItems())).thenReturn(List.of(snapshot));
            when(orderRepository.save(any())).thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId("order-1");
                order.setPaymentStatus(PaymentStatus.UNPAID);
                return order;
            });
        }

        private PlaceOrderCommand command(String promotionCode) {
            return PlaceOrderCommand.builder()
                .userId("user-1")
                .promotionCode(promotionCode)
                .shipFullName("User")
                .shipPhone("0123")
                .shipAddress("123 Street")
                .shipCity("HCM")
                .shipDistrict("1")
                .shipWard("Ward")
                .shippingMode(ShippingMode.STANDARD)
                .shippingFee(BigDecimal.TEN)
                .paymentMethod(PaymentMethod.COD)
                .build();
        }
    }
}
