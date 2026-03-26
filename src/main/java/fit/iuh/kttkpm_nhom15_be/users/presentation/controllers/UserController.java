package fit.iuh.kttkpm_nhom15_be.users.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.users.application.commands.CreateUserCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.commands.UpdateProfileCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.commands.UpdateUserCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.UserResponse;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.*;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.*;
import fit.iuh.kttkpm_nhom15_be.users.presentation.requests.CreateUserRequest;
import fit.iuh.kttkpm_nhom15_be.users.presentation.requests.UpdateProfileRequest;
import fit.iuh.kttkpm_nhom15_be.users.presentation.requests.UpdateUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ToggleUserStatusUseCase toggleUserStatusUseCase;
    private final GetUsersUseCase getUsersUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;

    @PostMapping
    public ResponseEntity<Map<String, String>> createUser(@Valid @RequestBody CreateUserRequest req) {
        String id = createUserUseCase.execute(new CreateUserCommand(
                req.getEmail(), req.getPhone(), req.getPassword(), req.getFullName(), req.getRole()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("userId", id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable String id, @Valid @RequestBody UpdateUserRequest req) {
        updateUserUseCase.execute(new UpdateUserCommand(
                id, req.getEmail(), req.getPhone(), req.getFullName(), req.getRole()
        ));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader("X-User-Id") String userId,
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        deleteUserUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{targetId}/toggle-status")
    public ResponseEntity<Void> toggleStatus(
            @RequestHeader("X-User-Id") String adminId, // Giả lập lấy ID Admin hiện tại
            @PathVariable String targetId) {
        toggleUserStatusUseCase.execute(adminId, targetId);
        return ResponseEntity.ok().build();
    }

    // --- Exception Handlers ---
    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(DuplicateUserException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ActionNotAllowedException.class)
    public ResponseEntity<Map<String, String>> handleNotAllowed(ActionNotAllowedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<UserResponse> response = getUsersUseCase.execute(keyword, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        UserResponse response = getUserByIdUseCase.execute(id);
        return ResponseEntity.ok(response);
    }
}
