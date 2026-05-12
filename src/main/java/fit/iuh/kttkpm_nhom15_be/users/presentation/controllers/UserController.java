package fit.iuh.kttkpm_nhom15_be.users.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.users.application.commands.UpdateProfileCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.UserResponse;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.*;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.MessageResponse;
import fit.iuh.kttkpm_nhom15_be.users.presentation.requests.UpdateProfileRequest;
import fit.iuh.kttkpm_nhom15_be.users.presentation.requests.UpdateUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody UpdateProfileRequest req
    ) {
        UserResponse response = updateProfileUseCase.execute(new UpdateProfileCommand(
                userId,
                req.getEmail(),
                req.getPhone(),
                req.getFullName(),
                req.getAvatar()
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
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(getUsersUseCase.execute(keyword, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(getUserByIdUseCase.execute(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable String id) {
        deleteUserUseCase.execute(id);
        return ResponseEntity.ok(new MessageResponse("Nguoi dung da duoc xoa thanh cong"));
    }

    @PutMapping("/{id}")
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
    public ResponseEntity<MessageResponse> toggleUserStatus(
            @AuthenticationPrincipal String adminId,
            @PathVariable("id") String targetUserId
    ) {
        toggleUserStatusUseCase.execute(adminId, targetUserId);
        return ResponseEntity.ok(new MessageResponse("Trang thai nguoi dung da duoc cap nhat thanh cong"));
    }
}
