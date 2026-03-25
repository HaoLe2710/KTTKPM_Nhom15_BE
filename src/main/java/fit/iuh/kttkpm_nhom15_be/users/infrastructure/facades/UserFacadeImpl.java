package fit.iuh.kttkpm_nhom15_be.users.infrastructure.facades;

import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserFacadeImpl implements UserFacade {

    private final UserRepository userRepository;

    @Override
    public boolean isUserActive(String userId) {
        return userRepository.findById(userId)
                // Nếu tìm thấy user, trả về trạng thái isActive của user đó
                .map(user -> user.isActive())
                // Nếu không tìm thấy ID này trong DB, mặc định coi như không hoạt động (false)
                .orElse(false);
    }
}