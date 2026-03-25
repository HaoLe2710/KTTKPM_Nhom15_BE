package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.dto.UserResponse;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.UserNotFoundException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserByIdUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse execute(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return UserResponse.fromDomain(user);
    }
}