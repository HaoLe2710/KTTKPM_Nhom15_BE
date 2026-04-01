package fit.iuh.kttkpm_nhom15_be.auth.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.auth.application.usecases.LoginUseCase;
import fit.iuh.kttkpm_nhom15_be.auth.application.usecases.RegisterUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        registerUseCase.execute(
                request.get("email"),
                request.get("phone"),
                request.get("password"),
                request.get("fullName")
        );
        return ResponseEntity.ok(Map.of("message", "Đăng ký tài khoản thành công!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String token = loginUseCase.execute(request.get("identifier"), request.get("password"));
        return ResponseEntity.ok(Map.of("accessToken", token, "tokenType", "Bearer"));
    }
}