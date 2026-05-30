package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.auth.application.services.OtpService;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.ActionNotAllowedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.DuplicateUserException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.UserNotFoundException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfirmOldEmailChangeUseCase {

    private final UserRepository userRepository;
    private final OtpService otpService;

    public void execute(String userId, String newEmail, String oldEmailOtp) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Không tìm thấy tài khoản để đổi email."));

        if (user.getEmail().equals(newEmail)) {
            throw new ActionNotAllowedException("Email mới trùng với email hiện tại.");
        }

        User existingUser = userRepository.findByEmail(newEmail).orElse(null);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new DuplicateUserException("Email này đã được sử dụng bởi tài khoản khác!");
        }

        boolean oldEmailVerified = otpService.verifyOtp(user.getEmail(), oldEmailOtp, "UPDATE_EMAIL_OLD");
        if (!oldEmailVerified) {
            throw new ActionNotAllowedException("OTP xác thực email cũ không đúng hoặc đã hết hạn!");
        }

        otpService.sendOtp(newEmail, "UPDATE_EMAIL_NEW");
    }
}
