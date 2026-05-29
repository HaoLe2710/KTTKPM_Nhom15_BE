package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.auth.application.services.OtpService;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.UserResponse;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.ActionNotAllowedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.DuplicateUserException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class VerifyUpdateEmailUseCaseTest {

    private UserRepository userRepository;
    private OtpService otpService;
    private VerifyUpdateEmailUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        otpService = mock(OtpService.class);
        useCase = new VerifyUpdateEmailUseCase(userRepository, otpService);
    }

    @Test
    void executeUpdatesEmailWhenNewOtpValid() {
        User user = User.builder().id("user-1").email("old@example.com").build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(otpService.verifyOtp("new@example.com", "123456", "UPDATE_EMAIL_NEW")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = useCase.execute("user-1", "new@example.com", "123456");

        assertEquals("new@example.com", response.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void executeThrowsWhenOtpInvalid() {
        User user = User.builder().id("user-1").email("old@example.com").build();
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(otpService.verifyOtp("new@example.com", "000000", "UPDATE_EMAIL_NEW")).thenReturn(false);

        assertThrows(ActionNotAllowedException.class, () ->
                useCase.execute("user-1", "new@example.com", "000000")
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void executeThrowsWhenNewEmailUsedByAnotherUser() {
        User user = User.builder().id("user-1").email("old@example.com").build();
        User another = User.builder().id("user-2").email("new@example.com").build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(another));

        assertThrows(DuplicateUserException.class, () ->
                useCase.execute("user-1", "new@example.com", "123456")
        );

        verify(otpService, never()).verifyOtp(eq("new@example.com"), any(), any());
    }
}
