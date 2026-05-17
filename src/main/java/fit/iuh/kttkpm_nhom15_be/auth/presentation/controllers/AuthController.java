package fit.iuh.kttkpm_nhom15_be.auth.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.auth.application.services.OtpService;
import fit.iuh.kttkpm_nhom15_be.auth.application.usecases.LoginUseCase;
import fit.iuh.kttkpm_nhom15_be.auth.application.usecases.RegisterUseCase;
import fit.iuh.kttkpm_nhom15_be.auth.domain.exceptions.InvalidOtpException;
import fit.iuh.kttkpm_nhom15_be.auth.presentation.requests.VerifyOtpRequest;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.advice.SkipSuccessEnvelope;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.MessageResponse;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.RegisterRequest;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.UserResponse;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final OtpService otpService;
    private final UserFacade userFacade;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        registerUseCase.execute(request);
        otpService.sendOtp(request.email(), "REGISTER");
        return ResponseEntity.ok(new MessageResponse("Dang ky thanh cong. Vui long kiem tra ma OTP trong email cua ban."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<MessageResponse> verify(@Valid @RequestBody VerifyOtpRequest request) {
        boolean isValid = otpService.verifyOtp(request.email(), request.otp(), "REGISTER");
        if (isValid) {
            userFacade.activateUser(request.email());
            return ResponseEntity.ok(new MessageResponse("Tai khoan da duoc kich hoat thanh cong!"));
        }
        throw new InvalidOtpException("Ma OTP khong dung hoac da het han.");
    }

    @PostMapping("/login")
    @SkipSuccessEnvelope
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String identifier = request.get("identifier");
        if (identifier == null || identifier.isBlank()) {
            identifier = request.get("email");
        }
        if (identifier == null || identifier.isBlank()) {
            identifier = request.get("phone");
        }

        String token = loginUseCase.execute(identifier, request.get("password"));
        User user = userFacade.findByIdentifier(identifier)
                .orElseThrow(() -> new IllegalStateException("Khong tim thay thong tin nguoi dung sau khi dang nhap"));
        UserResponse userResponse = UserResponse.fromDomain(user);

        return ResponseEntity.ok(Map.of(
                "accessToken", token,
                "tokenType", "Bearer",
                "userId", userResponse.id(),
                "role", userResponse.role(),
                "user", userResponse
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletResponse response) {
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

        return ResponseEntity.ok(new MessageResponse("Dang xuat thanh cong"));
    }
}
