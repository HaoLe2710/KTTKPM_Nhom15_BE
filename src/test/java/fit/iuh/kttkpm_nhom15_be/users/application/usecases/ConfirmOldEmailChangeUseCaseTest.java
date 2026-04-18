package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.auth.application.services.OtpService;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.ActionNotAllowedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.DuplicateUserException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ConfirmOldEmailChangeUseCaseTest {

    private UserRepository userRepository;
    private OtpService otpService;
    private ConfirmOldEmailChangeUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        otpService = mock(OtpService.class);
        useCase = new ConfirmOldEmailChangeUseCase(userRepository, otpService);
    }

    @Test
    void executeVerifiesOldEmailAndSendsOtpToNewEmail() {
        User user = User.builder().id("user-1").email("old@example.com").build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(otpService.verifyOtp("old@example.com", "123456", "UPDATE_EMAIL_OLD")).thenReturn(true);

        useCase.execute("user-1", "new@example.com", "123456");

        verify(otpService).sendOtp(eq("new@example.com"), eq("UPDATE_EMAIL_NEW"));
    }

    @Test
    void executeThrowsWhenOldOtpInvalid() {
        User user = User.builder().id("user-1").email("old@example.com").build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(otpService.verifyOtp("old@example.com", "999999", "UPDATE_EMAIL_OLD")).thenReturn(false);

        assertThrows(ActionNotAllowedException.class, () ->
                useCase.execute("user-1", "new@example.com", "999999")
        );

        verify(otpService, never()).sendOtp(any(), any());
    }

    @Test
    void executeThrowsWhenNewEmailUsedByAnotherUser() {
        User user = User.builder().id("user-1").email("old@example.com").build();
        User anotherUser = User.builder().id("user-2").email("new@example.com").build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(anotherUser));

        assertThrows(DuplicateUserException.class, () ->
                useCase.execute("user-1", "new@example.com", "123456")
        );

        verify(otpService, never()).verifyOtp(any(), any(), any());
        verify(otpService, never()).sendOtp(any(), any());
    }
}
