package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.commands.UpdateProfileCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.UserResponse;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.DuplicateUserException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.UserNotFoundException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProfileUseCase {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse execute(UpdateProfileCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        if (userRepository.existsByEmailOrPhoneExcludingId(command.email(), command.phone(), command.userId())) {
            throw new DuplicateUserException("Email hoặc SĐT đã bị người khác sử dụng.");
        }

        user.setEmail(command.email());
        user.setPhone(command.phone());
        user.setFullName(command.fullName());
        user.setAvatarUrl(command.avatarUrl());

        User savedUser = userRepository.save(user);
        return UserResponse.fromDomain(savedUser);
    }
}
