package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.auth.application.services.OtpService;
import fit.iuh.kttkpm_nhom15_be.users.application.commands.UpdateProfileCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.UserResponse;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.ActionNotAllowedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.DuplicateUserException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.UserNotFoundException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class UpdateProfileUseCaseTest {

    private UserRepository userRepository;
    private OtpService otpService;
    private UpdateProfileUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        otpService = Mockito.mock(OtpService.class);
        // Fix lỗi "Expected 2 arguments" ở đây nè sếp!
        useCase = new UpdateProfileUseCase(userRepository, otpService);
    }

    @Test
    void executeUpdatesProfileSuccessfully_WhenEmailUnchanged() {
        // GIVEN: Email không đổi, chỉ đổi tên và avatar
        User user = User.builder()
                .id("user-1")
                .email("old@example.com")
                .phone("0909000001")
                .fullName("Old Name")
                .avatarUrl("https://old-avatar")
                .build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        UserResponse result = useCase.execute(new UpdateProfileCommand(
                "user-1",
                "old@example.com", // Giữ nguyên email
                "0909000001",
                "Nguyen Van A",
                "https://cdn.example/avatar.png"
        ));

        // THEN
        verify(userRepository).save(any(User.class));
        assertEquals("Nguyen Van A", result.fullName());
        assertEquals("https://cdn.example/avatar.png", result.avatarUrl());
        verify(otpService, never()).sendOtp(any(), any(), any());
    }

    @Test
    void executeThrowsExceptionAndSendsOtp_WhenEmailChanged() {
        // GIVEN
        User user = User.builder()
                .id("user-1")
                .email("old@example.com")
                .build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        // WHEN & THEN
        ActionNotAllowedException ex = assertThrows(ActionNotAllowedException.class, () ->
                useCase.execute(new UpdateProfileCommand(
                        "user-1",
                        "new@example.com", // Thay đổi email
                        "0909000001",
                        "Nguyen Van A",
                        null
                ))
        );

        assertTrue(ex.getMessage().contains("Mã xác thực đã gửi đến new@example.com"));
        // Kiểm tra xem có gọi sang OtpService để lưu DB V9 không
        verify(otpService).sendOtp(eq("user-1"), eq("new@example.com"), eq("UPDATE_EMAIL"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void executeThrowsExceptionWhenUserNotFound() {
        when(userRepository.findById("missing-user")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                useCase.execute(new UpdateProfileCommand("missing-user", "a@b.com", "09", "A", null))
        );
    }

    @Test
    void executeThrowsExceptionWhenPhoneAlreadyUsedByOther() {
        // GIVEN
        User user = User.builder().id("user-1").email("same@example.com").phone("01").build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        // Giả lập SĐT mới bị trùng
        when(userRepository.existsByEmailOrPhoneExcludingId("same@example.com", "0999", "user-1")).thenReturn(true);

        // WHEN & THEN
        assertThrows(DuplicateUserException.class, () ->
                useCase.execute(new UpdateProfileCommand("user-1", "same@example.com", "0999", "A", null))
        );
    }
}