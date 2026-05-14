package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.events.UserStatusChangedEvent;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.ActionNotAllowedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.UserNotFoundException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ToggleUserStatusUseCase {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void execute(String adminId, String targetUserId) {
        // 1. Không cho tự khóa mình
        if (adminId.equals(targetUserId)) {
            throw new ActionNotAllowedException("Bạn không thể tự vô hiệu hóa tài khoản của chính mình.");
        }

        // 2. Tìm tài khoản
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException(targetUserId));

        // 3. Đảo cờ trạng thái
        user.setActive(!user.isActive());
        userRepository.save(user);

        // 4. Bắn Event (ví dụ: để đá Token của user bị khóa văng ra ngoài)
        eventPublisher.publishEvent(new UserStatusChangedEvent(user.getId(), user.isActive()));
    }
}