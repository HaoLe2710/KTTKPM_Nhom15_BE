package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.auth.application.services.OtpService;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.UserResponse;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.ActionNotAllowedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.DuplicateUserException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.UserNotFoundException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyUpdateEmailUseCase {

    private final UserRepository userRepository;
    private final OtpService otpService;

    @Transactional
    public UserResponse execute(String userId, String newEmail, String otpCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy tài khoản để cập nhật."));

        User existingUser = userRepository.findByEmail(newEmail).orElse(null);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new DuplicateUserException("Email này đã được sử dụng bởi tài khoản khác!");
        }

        boolean isValid = otpService.verifyOtp(newEmail, otpCode, "UPDATE_EMAIL_NEW");
        if (!isValid) {
            throw new ActionNotAllowedException("Mã xác thực email mới không đúng hoặc đã hết hạn!");
        }

        user.setEmail(newEmail);
        User updatedUser = userRepository.save(user);

        return UserResponse.fromDomain(updatedUser);
    }
}
