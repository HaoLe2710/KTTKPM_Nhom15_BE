package fit.iuh.kttkpm_nhom15_be.auth.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.auth.application.usecases.LoginUseCase;
import fit.iuh.kttkpm_nhom15_be.auth.application.usecases.RegisterUseCase;
import fit.iuh.kttkpm_nhom15_be.auth.application.services.OtpService;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.RegisterRequest;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
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
                "message", "Dang ky thanh cong. Vui long kiem tra ma OTP trong email cua ban."
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verify(@RequestParam String email, @RequestParam String otp) {

        boolean isValid = otpService.verifyOtp(email, otp, "REGISTER");

        if (isValid) {
            userFacade.activateUser(email);
            return ResponseEntity.ok(Map.of("message", "Tai khoan da duoc kich hoat thanh cong!"));
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Ma OTP khong dung hoac da het han."));
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie clearAuthTokenCookie = ResponseCookie.from("AUTH-TOKEN", "")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        ResponseCookie clearAccessTokenCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, clearAuthTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearAccessTokenCookie.toString());

        return ResponseEntity.ok(Map.of("message", "Dang xuat thanh cong"));
    }
}
