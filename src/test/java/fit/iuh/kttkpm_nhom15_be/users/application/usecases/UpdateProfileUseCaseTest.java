package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.commands.UpdateProfileCommand;
import fit.iuh.kttkpm_nhom15_be.users.application.dto.UserResponse;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.DuplicateUserException;
import fit.iuh.kttkpm_nhom15_be.users.domain.exceptions.UserNotFoundException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.User;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.UserRole;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateProfileUseCaseTest {

    @Test
    void executeUpdatesProfileSuccessfully() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UpdateProfileUseCase useCase = new UpdateProfileUseCase(userRepository);

        User user = User.builder()
                .id("user-1")
                .email("old@example.com")
                .phone("0909000001")
                .fullName("Old Name")
                .avatarUrl("https://old-avatar")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .addresses(new ArrayList<>())
                .build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailOrPhoneExcludingId("new@example.com", "0909000009", "user-1")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse result = useCase.execute(new UpdateProfileCommand(
                "user-1",
                "new@example.com",
                "0909000009",
                "Nguyen Van A",
                "https://cdn.example/avatar.png"
        ));

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());

        assertEquals("new@example.com", savedUser.getValue().getEmail());
        assertEquals("0909000009", savedUser.getValue().getPhone());
        assertEquals("Nguyen Van A", savedUser.getValue().getFullName());
        assertEquals("https://cdn.example/avatar.png", savedUser.getValue().getAvatarUrl());
        assertEquals("Nguyen Van A", result.fullName());
        assertEquals("https://cdn.example/avatar.png", result.avatarUrl());
    }

    @Test
    void executeThrowsVietnameseExceptionWhenUserNotFound() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UpdateProfileUseCase useCase = new UpdateProfileUseCase(userRepository);

        when(userRepository.findById("missing-user")).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> useCase.execute(
                new UpdateProfileCommand("missing-user", "new@example.com", "0909", "A", null)
        ));

        assertEquals("Không tìm thấy tài khoản với ID: missing-user", ex.getMessage());
        verify(userRepository, never()).save(any());
        verify(userRepository, never()).existsByEmailOrPhoneExcludingId(any(), any(), any());
    }

    @Test
    void executeThrowsVietnameseExceptionWhenEmailOrPhoneAlreadyUsed() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UpdateProfileUseCase useCase = new UpdateProfileUseCase(userRepository);

        User user = User.builder()
                .id("user-1")
                .role(UserRole.CUSTOMER)
                .addresses(new ArrayList<>())
                .build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailOrPhoneExcludingId("new@example.com", "0909", "user-1")).thenReturn(true);

        DuplicateUserException ex = assertThrows(DuplicateUserException.class, () -> useCase.execute(
                new UpdateProfileCommand("user-1", "new@example.com", "0909", "A", null)
        ));

        assertEquals("Email hoặc SĐT đã bị người khác sử dụng.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void executeAllowsAvatarToBeNull() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UpdateProfileUseCase useCase = new UpdateProfileUseCase(userRepository);

        User user = User.builder()
                .id("user-1")
                .email("old@example.com")
                .phone("0909000001")
                .fullName("Old Name")
                .avatarUrl("https://old-avatar")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .addresses(new ArrayList<>())
                .build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailOrPhoneExcludingId("new@example.com", "0909000009", "user-1")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse result = useCase.execute(new UpdateProfileCommand(
                "user-1",
                "new@example.com",
                "0909000009",
                "Nguyen Van B",
                null
        ));

        assertEquals("Nguyen Van B", result.fullName());
        assertNull(result.avatarUrl());
    }

    @Test
    void executeKeepsUserRoleAndStatusUnchangedAfterProfileUpdate() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UpdateProfileUseCase useCase = new UpdateProfileUseCase(userRepository);

        User user = User.builder()
                .id("user-1")
                .email("old@example.com")
                .phone("0909000001")
                .fullName("Old Name")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .addresses(new ArrayList<>())
                .build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailOrPhoneExcludingId("new@example.com", "0909000009", "user-1")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new UpdateProfileCommand(
                "user-1",
                "new@example.com",
                "0909000009",
                "Nguyen Van A",
                "https://cdn.example/avatar.png"
        ));

        assertEquals(UserRole.CUSTOMER, user.getRole());
        assertEquals(true, user.isActive());
    }

    @Test
    void executeChecksDuplicateUsingCurrentUserId() {
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        UpdateProfileUseCase useCase = new UpdateProfileUseCase(userRepository);

        User user = User.builder()
                .id("user-99")
                .role(UserRole.CUSTOMER)
                .addresses(new ArrayList<>())
                .build();

        when(userRepository.findById("user-99")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailOrPhoneExcludingId("profile@example.com", "0911000000", "user-99")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(new UpdateProfileCommand(
                "user-99",
                "profile@example.com",
                "0911000000",
                "Profile User",
                null
        ));

        verify(userRepository).existsByEmailOrPhoneExcludingId("profile@example.com", "0911000000", "user-99");
    }
}
