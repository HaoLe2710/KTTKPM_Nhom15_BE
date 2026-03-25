package fit.iuh.kttkpm_nhom15_be.orders.infrastructure.facades;

import fit.iuh.kttkpm_nhom15_be.orders.application.dto.RawOrderStatsDTO;
import fit.iuh.kttkpm_nhom15_be.orders.application.interfaces.OrderFacade;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacadeImpl implements OrderFacade {

    private final OrderRepository orderRepository;

    @Override
    public RawOrderStatsDTO getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching order statistics between {} and {}", startDate, endDate);
        return orderRepository.getOrderStatistics(startDate, endDate);
    }

    @Override
    public boolean hasPromotionBeenUsed(String promotionId) {
        log.info("Checking whether promotion {} has been used by any order", promotionId);
        return orderRepository.existsByPromotionId(promotionId);
    }

    public boolean hasOrdersByUser(String userId) {
        return orderRepository.hasOrdersByUser(userId);
    }
}
