package fit.iuh.kttkpm_nhom15_be.users.presentation.controllers.admin;

import fit.iuh.kttkpm_nhom15_be.users.application.commands.CreateUserCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.usecases.CreateUserUseCase;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.UserRole;
import fit.iuh.kttkpm_nhom15_be.users.presentation.requests.CreateStaffRequest;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/staffs")
@RequiredArgsConstructor
public class AdminStaffController {

    private final CreateUserUseCase createUserUseCase;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> createStaff(@Valid @RequestBody CreateStaffRequest req) {
        String userId = createUserUseCase.execute(new CreateUserCommand(
                req.getEmail(),
                req.getPhone(),
                req.getPassword(),
                req.getFullName(),
                UserRole.STAFF
        ));
        return ResponseEntity.ok(Map.of(
                "id", userId,
                "role", UserRole.STAFF.name(),
                "message", "Tạo tài khoản nhân viên thành công"
        ));
    }
}

