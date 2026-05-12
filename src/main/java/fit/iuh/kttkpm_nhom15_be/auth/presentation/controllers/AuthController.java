package fit.iuh.kttkpm_nhom15_be.auth.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.auth.application.dto.AuthResponse;
import fit.iuh.kttkpm_nhom15_be.auth.application.usecases.LoginUseCase;
import fit.iuh.kttkpm_nhom15_be.auth.application.usecases.RegisterUseCase;
import fit.iuh.kttkpm_nhom15_be.auth.application.services.OtpService;
import fit.iuh.kttkpm_nhom15_be.auth.domain.exceptions.InvalidOtpException;
import fit.iuh.kttkpm_nhom15_be.auth.presentation.requests.LoginRequest;
import fit.iuh.kttkpm_nhom15_be.auth.presentation.requests.VerifyOtpRequest;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.advice.ApiSuccessMessage;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.responses.MessageResponse;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.RegisterRequest;
import fit.iuh.kttkpm_nhom15_be.users.application.interfaces.UserFacade;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

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

        return ResponseEntity.ok(new MessageResponse(
                "Dang ky thanh cong. Vui long kiem tra ma OTP trong email cua ban."
        ));
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
    @ApiSuccessMessage("Dang nhap thanh cong")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = loginUseCase.execute(request.identifier(), request.password());

        return ResponseEntity.ok(new AuthResponse(token, "Bearer"));
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
