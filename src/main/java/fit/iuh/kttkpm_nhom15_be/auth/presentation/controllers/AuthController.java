package fit.iuh.kttkpm_nhom15_be.auth.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.auth.application.usecases.LoginUseCase;
import fit.iuh.kttkpm_nhom15_be.auth.application.usecases.RegisterUseCase;
import fit.iuh.kttkpm_nhom15_be.auth.application.services.OtpService;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.RegisterRequest;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;

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
    private final OtpService otpService;
    private final UserFacade userFacade;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        registerUseCase.execute(request);

        otpService.sendOtp(null, request.email(), "REGISTER");

        return ResponseEntity.ok(Map.of(
                "message", "Đăng ký thành công. Vui lòng kiểm tra mã OTP trong email của bạn."
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verify(@RequestParam String email, @RequestParam String otp) {

        boolean isValid = otpService.verifyOtp(email, otp, "REGISTER");

        if (isValid) {
            // 2. Kích hoạt tài khoản
            userFacade.activateUser(email);
            return ResponseEntity.ok(Map.of("message", "Tài khoản đã được kích hoạt thành công!"));
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Mã OTP không đúng hoặc đã hết hạn."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String token = loginUseCase.execute(
                request.get("identifier"),
                request.get("password")
        );

        return ResponseEntity.ok(Map.of(
                "accessToken", token,
                "tokenType", "Bearer"
        ));
    }
}