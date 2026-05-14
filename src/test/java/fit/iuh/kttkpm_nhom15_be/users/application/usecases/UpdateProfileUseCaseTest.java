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
        userRepository = mock(UserRepository.class);
        otpService = mock(OtpService.class);
        useCase = new UpdateProfileUseCase(userRepository, otpService);
    }

    @Test
    void executeUpdatesProfileSuccessfullyWhenEmailUnchanged() {
        User user = User.builder()
                .id("user-1")
                .email("old@example.com")
                .phone("0909000001")
                .fullName("Old Name")
                .avatarUrl("https://old-avatar")
                .build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse result = useCase.execute(new UpdateProfileCommand(
                "user-1",
                "old@example.com",
                "0909000001",
                "Nguyen Van A",
                "https://cdn.example/avatar.png"
        ));

        verify(userRepository).save(any(User.class));
        assertEquals("Nguyen Van A", result.fullName());
        assertEquals("https://cdn.example/avatar.png", result.avatarUrl());
        verify(otpService, never()).sendOtp(any(), any());
    }

    @Test
    void executeThrowsAndSendsOtpToOldEmailWhenEmailChanged() {
        User user = User.builder()
                .id("user-1")
                .email("old@example.com")
                .phone("0909000001")
                .build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        ActionNotAllowedException ex = assertThrows(ActionNotAllowedException.class, () ->
                useCase.execute(new UpdateProfileCommand(
                        "user-1",
                        "new@example.com",
                        "0909000001",
                        "Nguyen Van A",
                        null
                ))
        );

        assertTrue(ex.getMessage().contains("old@example.com"));
        verify(otpService).sendOtp(eq("old@example.com"), eq("UPDATE_EMAIL_OLD"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void executeThrowsExceptionWhenNewEmailAlreadyUsed() {
        User currentUser = User.builder().id("user-1").email("old@example.com").phone("01").build();
        User otherUser = User.builder().id("user-2").email("new@example.com").phone("02").build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(currentUser));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(otherUser));

        assertThrows(DuplicateUserException.class, () ->
                useCase.execute(new UpdateProfileCommand("user-1", "new@example.com", "01", "A", null))
        );

        verify(otpService, never()).sendOtp(any(), any());
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
        User user = User.builder().id("user-1").email("same@example.com").phone("01").build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailOrPhoneExcludingId("same@example.com", "0999", "user-1")).thenReturn(true);

        assertThrows(DuplicateUserException.class, () ->
                useCase.execute(new UpdateProfileCommand("user-1", "same@example.com", "0999", "A", null))
        );
    }
}
