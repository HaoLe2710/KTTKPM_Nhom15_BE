package fit.iuh.kttkpm_nhom15_be.orders.application.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fit.iuh.kttkpm_nhom15_be.orders.application.dto.OrderHistoryItemDTO;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class GetMyOrderHistoryUseCaseTest {

  @Test
  void getMyOrderHistoryReturnsRepositoryResult() {
    OrderRepository orderRepository = mock(OrderRepository.class);
    GetMyOrderHistoryUseCase useCase = new GetMyOrderHistoryUseCase(orderRepository);
    List<OrderHistoryItemDTO> expected = List.of(
      new OrderHistoryItemDTO(
        "order-1",
        "ORD-1",
        PaymentMethod.COD,
        PaymentStatus.UNPAID,
        OrderStatus.CREATED,
        "Nguyen Van A",
        BigDecimal.valueOf(250000),
        LocalDateTime.of(2026, 5, 28, 10, 0)
      )
    );

    when(orderRepository.findOrderHistoryByUserId("user-1")).thenReturn(expected);

    assertEquals(expected, useCase.execute("user-1"));
  }
}
