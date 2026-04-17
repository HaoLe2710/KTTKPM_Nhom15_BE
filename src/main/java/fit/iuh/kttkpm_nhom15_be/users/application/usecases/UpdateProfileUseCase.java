package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.auth.application.services.OtpService;
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

    @Transactional
    public UserResponse execute(UpdateProfileCommand command) {

        // 1. Tìm User (Domain Model)
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException("User không tồn tại"));

        // 2. Kiểm tra nếu có ý định thay đổi Email
        if (!user.getEmail().equals(command.email())) {
            otpService.sendOtp(user.getId(), command.email(), "UPDATE_EMAIL");

            throw new ActionNotAllowedException("Yêu cầu đổi email đã được ghi nhận. Vui lòng kiểm tra mã OTP gửi đến " + command.email());
        }

        // 3. Kiểm tra trùng SĐT (nếu phone thay đổi)
        if (!user.getPhone().equals(command.phone())) {
            if (userRepository.existsByEmailOrPhoneExcludingId(command.email(), command.phone(), command.userId())) {
                throw new DuplicateUserException("Số điện thoại này đã được sử dụng bởi tài khoản khác!");
            }
        }

        user.setAvatarUrl(command.avatarUrl());
        user.setFullName(command.fullName());
        user.setPhone(command.phone());

        User updatedUser = userRepository.save(user);

        return UserResponse.fromDomain(updatedUser);
    }
}