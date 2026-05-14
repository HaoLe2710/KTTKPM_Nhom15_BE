package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.commands.UpdateUserCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.events.UserUpdatedEvent;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.DuplicateUserException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.UserNotFoundException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void execute(UpdateUserCommand command) {
        // 1. Kiểm tra tồn tại
        User user = userRepository.findById(command.id())
                .orElseThrow(() -> new UserNotFoundException(command.id()));

        // 2. Kiểm tra trùng Email/SĐT (loại trừ chính user đang sửa)
        if (userRepository.existsByEmailOrPhoneExcludingId(command.email(), command.phone(), command.id())) {
            throw new DuplicateUserException("Email hoặc SĐT đã bị người khác sử dụng.");
        }

        // 3. Cập nhật thông tin
        user.setEmail(command.email());
        user.setPhone(command.phone());
        user.setFullName(command.fullName());
        user.setRole(command.role());

        userRepository.save(user);

        // 4. Bắn Event
        eventPublisher.publishEvent(new UserUpdatedEvent(user.getId()));
    }
}