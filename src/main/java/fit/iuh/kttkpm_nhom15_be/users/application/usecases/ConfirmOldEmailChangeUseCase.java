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
                .orElseThrow(() -> new UserNotFoundException("Khong tim thay tai khoan de doi email."));

        if (user.getEmail().equals(newEmail)) {
            throw new ActionNotAllowedException("Email moi trung voi email hien tai.");
        }

        User existingUser = userRepository.findByEmail(newEmail).orElse(null);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new DuplicateUserException("Email nay da duoc su dung boi tai khoan khac!");
        }

        boolean oldEmailVerified = otpService.verifyOtp(user.getEmail(), oldEmailOtp, "UPDATE_EMAIL_OLD");
        if (!oldEmailVerified) {
            throw new ActionNotAllowedException("OTP xac thuc email cu khong dung hoac da het han!");
        }

        otpService.sendOtp(newEmail, "UPDATE_EMAIL_NEW");
    }
}
