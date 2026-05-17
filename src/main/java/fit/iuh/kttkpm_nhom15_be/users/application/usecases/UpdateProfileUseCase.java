package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.auth.application.services.OtpService;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.FileStoragePort;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.StoredFile;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.UploadFileCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.commands.UpdateProfileCommand;
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
public class UpdateProfileUseCase {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final FileStoragePort fileStoragePort;

    @Transactional
    public UserResponse execute(UpdateProfileCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("User khong ton tai"));

        if (!user.getEmail().equals(command.email())) {
            User existingUser = userRepository.findByEmail(command.email()).orElse(null);
            if (existingUser != null && !existingUser.getId().equals(command.userId())) {
                throw new DuplicateUserException("Email nay da duoc su dung boi tai khoan khac!");
            }

            // Step 1: verify current (old) email first.
            otpService.sendOtp(user.getEmail(), "UPDATE_EMAIL_OLD");

            throw new ActionNotAllowedException(
                    "Yeu cau doi email da duoc ghi nhan. Vui long xac thuc OTP gui den email cu: " + user.getEmail()
            );
        }

        if (!user.getPhone().equals(command.phone())
                && userRepository.existsByEmailOrPhoneExcludingId(command.email(), command.phone(), command.userId())) {
            throw new DuplicateUserException("So dien thoai nay da duoc su dung boi tai khoan khac!");
        }

        user.setAvatarUrl(resolveAvatarUrl(command, user.getAvatarUrl(), command.userId()));
        user.setFullName(command.fullName());
        user.setPhone(command.phone());

        User updatedUser = userRepository.save(user);

        return UserResponse.fromDomain(updatedUser);
    }

    private String resolveAvatarUrl(UpdateProfileCommand command, String fallbackAvatarUrl, String userId) {
        if (command.avatarBytes() != null && command.avatarBytes().length > 0) {
            StoredFile storedFile = fileStoragePort.upload(new UploadFileCommand(
                    "users/avatars/" + userId,
                    command.avatarOriginalFilename(),
                    command.avatarContentType(),
                    command.avatarBytes()
            ));
            return storedFile.url();
        }
        if (command.avatarUrl() != null && !command.avatarUrl().isBlank()) {
            return command.avatarUrl().trim();
        }
        return fallbackAvatarUrl;
    }
}
