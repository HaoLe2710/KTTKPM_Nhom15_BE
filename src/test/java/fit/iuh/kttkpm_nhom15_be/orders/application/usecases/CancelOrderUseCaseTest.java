package fit.iuh.kttkpm_nhom15_be.orders.application.usecases;

import fit.iuh.kttkpm_nhom15_be.catalog.application.interfaces.CatalogFacade;
import fit.iuh.kttkpm_nhom15_be.orders.application.commands.CancelOrderCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.StockRestoreItem;
import fit.iuh.kttkpm_nhom15_be.orders.application.events.OrderCancelledEvent;
import fit.iuh.kttkpm_nhom15_be.orders.application.events.ProductSalesChangedEvent;
import fit.iuh.kttkpm_nhom15_be.orders.application.results.CancelOrderResult;
import fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions.InvalidOrderStateTransitionException;
import fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions.OrderNotFoundException;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderItem;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CancelOrderUseCaseTest {

    @Test
    void executeCancelsOrderRestoresStockAndPublishesEvent() {
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        CatalogFacade catalogFacade = Mockito.mock(CatalogFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        CancelOrderUseCase useCase = new CancelOrderUseCase(orderRepository, catalogFacade, eventPublisher);
        Order order = cancellableOrder(OrderStatus.CREATED);

        when(orderRepository.findByIdAndUserIdForUpdate("order-1", "user-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        CancelOrderResult result = useCase.execute(new CancelOrderCommand("order-1", "user-1", "Customer changed mind"));

        ArgumentCaptor<List<StockRestoreItem>> restoreItems = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<OrderCancelledEvent> eventCaptor = ArgumentCaptor.forClass(OrderCancelledEvent.class);

        verify(catalogFacade).restoreStock(restoreItems.capture());
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        verify(eventPublisher).publishEvent(any(ProductSalesChangedEvent.class));
        verify(orderRepository).save(order);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals("variant-1", restoreItems.getValue().get(0).variantId());
        assertEquals(2, restoreItems.getValue().get(0).quantity());
        assertEquals("order-1", eventCaptor.getValue().orderId());
        assertEquals("customer@example.com", eventCaptor.getValue().recipientEmail());
        assertEquals("Customer changed mind", eventCaptor.getValue().reason());
        assertEquals("CANCELLED", result.getStatus());
    }

    @Test
    void executeThrowsWhenOrderDoesNotExist() {
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        CatalogFacade catalogFacade = Mockito.mock(CatalogFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        CancelOrderUseCase useCase = new CancelOrderUseCase(orderRepository, catalogFacade, eventPublisher);

        when(orderRepository.findByIdAndUserIdForUpdate("missing", "user-1")).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> useCase.execute(new CancelOrderCommand("missing", "user-1", "No order")));
        verify(orderRepository, never()).save(any(Order.class));
        verify(catalogFacade, never()).restoreStock(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void executeStopsWhenOrderStateCannotBeCancelled() {
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        CatalogFacade catalogFacade = Mockito.mock(CatalogFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        CancelOrderUseCase useCase = new CancelOrderUseCase(orderRepository, catalogFacade, eventPublisher);
        Order order = cancellableOrder(OrderStatus.SHIPPING);

        when(orderRepository.findByIdAndUserIdForUpdate("order-1", "user-1")).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStateTransitionException.class,
            () -> useCase.execute(new CancelOrderCommand("order-1", "user-1", "Too late")));
        verify(orderRepository, never()).save(any(Order.class));
        verify(catalogFacade, never()).restoreStock(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void executeSkipsRestoreForLegacyOrdersWithoutStockDeduction() {
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        CatalogFacade catalogFacade = Mockito.mock(CatalogFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        CancelOrderUseCase useCase = new CancelOrderUseCase(orderRepository, catalogFacade, eventPublisher);
        Order order = cancellableOrder(OrderStatus.CREATED);
        order.setStockDeducted(false);

        when(orderRepository.findByIdAndUserIdForUpdate("order-1", "user-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        useCase.execute(new CancelOrderCommand("order-1", "user-1", "Legacy order"));

        verify(catalogFacade, never()).restoreStock(any());
        verify(orderRepository).save(order);
    }

    @Test
    void executeRejectsPaidOrders() {
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        CatalogFacade catalogFacade = Mockito.mock(CatalogFacade.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        CancelOrderUseCase useCase = new CancelOrderUseCase(orderRepository, catalogFacade, eventPublisher);
        Order order = cancellableOrder(OrderStatus.CREATED);
        order.setPaymentStatus(PaymentStatus.PAID);

        when(orderRepository.findByIdAndUserIdForUpdate("order-1", "user-1")).thenReturn(Optional.of(order));

        assertThrows(ApiValidationException.class,
            () -> useCase.execute(new CancelOrderCommand("order-1", "user-1", "Already paid")));
        verify(orderRepository, never()).save(any(Order.class));
        verify(catalogFacade, never()).restoreStock(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private Order cancellableOrder(OrderStatus status) {
        return Order.builder()
            .id("order-1")
            .orderNo("ORD-001")
            .userId("user-1")
            .status(status)
            .paymentStatus(PaymentStatus.UNPAID)
            .stockDeducted(true)
            .totalAmount(new BigDecimal("220.00"))
            .shipEmail("customer@example.com")
            .shipFullName("Customer")
            .shipPhone("0901234567")
            .shipAddress("123 Street")
            .shipWard("Ward 1")
            .shipDistrict("District 1")
            .shipCity("HCM")
            .items(List.of(OrderItem.builder()
                .productId("product-1")
                .variantId("variant-1")
                .quantity(2)
                .build()))
            .build();
    }
}
