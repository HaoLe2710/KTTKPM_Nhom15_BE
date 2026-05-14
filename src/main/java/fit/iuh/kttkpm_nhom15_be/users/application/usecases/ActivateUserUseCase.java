package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.UserNotFoundException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivateUserUseCase {
    private final UserRepository userRepository;

    @Transactional
    public void execute(String email) {
        User user = userRepository.findByEmailIgnoreActive(email)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy người dùng với email: " + email));

        user.setActive(true);

        userRepository.save(user);
    }
}