package fit.iuh.kttkpm_nhom15_be.orders.application.interfaces;

import fit.iuh.kttkpm_nhom15_be.orders.application.dto.RawOrderStatsDTO;
import java.time.LocalDateTime;

public interface OrderFacade {
    RawOrderStatsDTO getOrderStatistics(LocalDateTime startDate, LocalDateTime endDate);
}
