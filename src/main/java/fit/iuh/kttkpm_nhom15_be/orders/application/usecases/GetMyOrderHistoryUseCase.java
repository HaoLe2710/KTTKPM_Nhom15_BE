package fit.iuh.kttkpm_nhom15_be.orders.application.usecases;

import fit.iuh.kttkpm_nhom15_be.orders.application.dto.OrderHistoryItemDTO;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMyOrderHistoryUseCase {

  private final OrderRepository orderRepository;

  public List<OrderHistoryItemDTO> execute(String userId) {
    return orderRepository.findOrderHistoryByUserId(userId);
  }
}
