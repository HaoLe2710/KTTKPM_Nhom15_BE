package fit.iuh.kttkpm_nhom15_be.orders.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.orders.application.dto.admin.OrderAdminDtos.OrderAdminDetailResponse;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.admin.OrderAdminDtos.OrderAdminSummaryRow;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface OrderAdminRepository {

  Page<OrderAdminSummaryRow> findOrders(String query,
                                        OrderStatus status,
                                        PaymentStatus paymentStatus,
                                        AdminPageRequest pageRequest);

  Optional<OrderAdminDetailResponse> findOrderDetail(String orderId);
}

