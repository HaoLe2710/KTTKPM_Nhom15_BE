package fit.iuh.kttkpm_nhom15_be.users.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.MessageResponse;
import fit.iuh.kttkpm_nhom15_be.users.application.commands.UpdateProfileCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.UserResponse;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.ConfirmOldEmailChangeUseCase;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.DeleteUserUseCase;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.GetUserByIdUseCase;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.GetUsersUseCase;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.ToggleUserStatusUseCase;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.UpdateProfileUseCase;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.UpdateUserUseCase;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.VerifyUpdateEmailUseCase;
import fit.iuh.kttkpm_nhom15_be.users.presentation.requests.UpdateProfileRequest;
import fit.iuh.kttkpm_nhom15_be.users.presentation.requests.UpdateUserRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ToggleUserStatusUseCase toggleUserStatusUseCase;
    private final GetUsersUseCase getUsersUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final ConfirmOldEmailChangeUseCase confirmOldEmailChangeUseCase;
    private final VerifyUpdateEmailUseCase verifyUpdateEmailUseCase;

    @PutMapping(value = "/profile", consumes = "application/json")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody UpdateProfileRequest req
    ) {
        UserResponse response = updateProfileUseCase.execute(new UpdateProfileCommand(
                userId,
                req.getEmail(),
                req.getPhone(),
                req.getFullName(),
                req.getAvatar(),
                null,
                null,
                null
        ));
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/profile", consumes = "multipart/form-data")
    public ResponseEntity<UserResponse> updateProfileMultipart(
            @AuthenticationPrincipal String userId,
            @Valid @ModelAttribute UpdateProfileRequest req
    ) {
        byte[] avatarBytes = null;
        String avatarOriginalFilename = null;
        String avatarContentType = null;
        if (req.getAvatarFile() != null && !req.getAvatarFile().isEmpty()) {
            try {
                avatarBytes = req.getAvatarFile().getBytes();
                avatarOriginalFilename = req.getAvatarFile().getOriginalFilename();
                avatarContentType = req.getAvatarFile().getContentType();
            } catch (java.io.IOException ex) {
                throw new IllegalStateException("Khong the doc du lieu file avatar", ex);
            }
        }

        UserResponse response = updateProfileUseCase.execute(new UpdateProfileCommand(
                userId,
                req.getEmail(),
                req.getPhone(),
                req.getFullName(),
                req.getAvatar(),
                avatarOriginalFilename,
                avatarContentType,
                avatarBytes
        ));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/confirm/email-change/old")
    public ResponseEntity<Map<String, String>> confirmOldEmail(
            @AuthenticationPrincipal String userId,
            @RequestParam String newEmail,
            @RequestParam("otp") String oldEmailOtp
    ) {
        confirmOldEmailChangeUseCase.execute(userId, newEmail, oldEmailOtp);
        return ResponseEntity.ok(Map.of(
                "message",
                "Xac thuc email cu thanh cong. OTP da duoc gui den email moi: " + newEmail
        ));
    }

    @PostMapping("/confirm/email-change")
    public ResponseEntity<UserResponse> verifyEmailChange(
            @AuthenticationPrincipal String userId,
            @RequestParam String newEmail,
            @RequestParam String otp
    ) {
        return ResponseEntity.ok(verifyUpdateEmailUseCase.execute(userId, newEmail, otp));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(getUsersUseCase.execute(keyword, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(getUserByIdUseCase.execute(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable String id) {
        deleteUserUseCase.execute(id);
        return ResponseEntity.ok(new MessageResponse("Nguoi dung da duoc xoa thanh cong"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest req
    ) {
        updateUserUseCase.execute(new fit.iuh.kttkpm_nhom15_be.users.application.commands.UpdateUserCommand(
                id,
                req.getEmail(),
                req.getPhone(),
                req.getFullName(),
                req.getRole()
        ));
        return ResponseEntity.ok(new MessageResponse("Thong tin nguoi dung da duoc cap nhat thanh cong"));
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> toggleUserStatus(
            @AuthenticationPrincipal String adminId,
            @PathVariable("id") String targetUserId
    ) {
        toggleUserStatusUseCase.execute(adminId, targetUserId);
        return ResponseEntity.ok(new MessageResponse("Trang thai nguoi dung da duoc cap nhat thanh cong"));
    }
}
