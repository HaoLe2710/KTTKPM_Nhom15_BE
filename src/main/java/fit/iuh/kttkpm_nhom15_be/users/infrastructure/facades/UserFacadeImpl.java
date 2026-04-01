package fit.iuh.kttkpm_nhom15_be.users.infrastructure.facades;

import fit.iuh.kttkpm_nhom15_be.users.application.commands.CreateUserCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.CreateUserUseCase;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.UserRole;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserFacadeImpl implements UserFacade {
    private final UserRepository userRepository;
    private final CreateUserUseCase createUserUseCase;

    @Override
    public boolean isUserActive(String userId) {
        return userRepository.findById(userId)
                .map(User::isActive)
                .orElse(false);
    }

    @Override
    public Optional<User> findByIdentifier(String identifier) {
        // Tìm theo email hoặc phone từ UserRepository sếp đã có
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier));
    }

    @Override
    public void registerCustomer(String email, String phone, String password, String fullName) {
        // Sử dụng CreateUserCommand có sẵn trong module users
        CreateUserCommand command = new CreateUserCommand(email, phone, password, fullName, UserRole.CUSTOMER);
        createUserUseCase.execute(command);
    }
}