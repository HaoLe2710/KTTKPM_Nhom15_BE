package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.commands.CreateUserCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.events.UserCreatedEvent;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.DuplicateUserException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder; // MỞ RA LẠI
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordEncoder passwordEncoder; // MỞ RA LẠI ĐỂ SỬ DỤNG

    @Transactional
    public String execute(CreateUserCommand command) {
        // 1. Kiểm tra trùng lặp
        if (userRepository.existsByEmailOrPhone(command.email(), command.phone())) {
            throw new DuplicateUserException("Email hoặc SĐT đã tồn tại.");
        }

        // 2. Map sang Domain Model + MÃ HÓA MẬT KHẨU
        User user = User.builder()
                .email(command.email())
                .phone(command.phone())
                // SỬ DỤNG passwordEncoder ĐỂ BĂM MẬT KHẨU Ở ĐÂY
                .password(passwordEncoder.encode(command.password()))
                .fullName(command.fullName())
                .role(command.role())
                .isActive(true)
                .addresses(new ArrayList<>())
                .build();

        // 3. Lưu xuống DB
        User saved = userRepository.save(user);

        // 4. Bắn Event bất đồng bộ
        eventPublisher.publishEvent(new UserCreatedEvent(saved.getId(), saved.getEmail()));

        return saved.getId();
    }
}