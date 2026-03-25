package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.orders.application.interfaces.OrderFacade;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.ActionNotAllowedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteUserUseCase {

    private final UserRepository userRepository;
    private final OrderFacade orderFacade; // Giao tiếp đồng bộ để check ràng buộc chéo

    @Transactional
    public void execute(String id) {
        // Ràng buộc Cross-module: Không xóa user đã có đơn hàng
        if (orderFacade.hasOrdersByUser(id)) {
            throw new ActionNotAllowedException("Không thể xóa do tài khoản đã có lịch sử giao dịch. Hãy vô hiệu hóa.");
        }

        userRepository.deleteById(id);
    }
}