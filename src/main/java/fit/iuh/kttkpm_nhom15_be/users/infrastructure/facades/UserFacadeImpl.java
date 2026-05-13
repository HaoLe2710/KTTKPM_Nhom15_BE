package fit.iuh.kttkpm_nhom15_be.users.infrastructure.facades;

import fit.iuh.kttkpm_nhom15_be.users.application.commands.CreateUserCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.RegisterRequest;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.ActivateUserUseCase;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.CreateUserUseCase;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.UserRole;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserFacadeImpl implements UserFacade {
    private final UserRepository userRepository;
    private final CreateUserUseCase createUserUseCase;
    private final PasswordEncoder passwordEncoder;
    private final ActivateUserUseCase activateUserUseCase;

    @Override
    public boolean isUserActive(String userId) {
        return userRepository.findById(userId)
                .map(User::isActive)
                .orElse(false);
    }

    @Override
    public Optional<User> findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }

        String normalized = identifier.trim();
        if (normalized.contains("@")) {
            normalized = normalized.toLowerCase(Locale.ROOT);
        }

        final String lookup = normalized;
        return userRepository.findByEmail(lookup)
                .or(() -> userRepository.findByPhone(lookup));
    }

    @Override
    public void registerCustomer(String email, String phone, String password, String fullName) {
        CreateUserCommand command = new CreateUserCommand(email, phone, password, fullName, UserRole.CUSTOMER);
        createUserUseCase.execute(command);
    }

    @Override
    public User createOAuth2User(String email, String fullName) {
        User user = User.builder()
                .email(email)
                .fullName(fullName)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }
    @Override
    public void registerNewUser(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setActive(false);
        user.setRole(UserRole.CUSTOMER);

        userRepository.save(user);
    }

    public void activateUser(String email) {
        activateUserUseCase.execute(email);
    }
}
