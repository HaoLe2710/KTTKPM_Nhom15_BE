package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CleanupUnverifiedUsersUseCaseTest {

    private UserRepository userRepository;
    private CleanupUnverifiedUsersUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        useCase = new CleanupUnverifiedUsersUseCase(userRepository);
        ReflectionTestUtils.setField(useCase, "unverifiedTtlMinutes", 15);
    }

    @Test
    void executeDeletesInactiveUsersOlderThanTtl() {
        when(userRepository.deleteInactiveUsersCreatedBefore(any(LocalDateTime.class))).thenReturn(3);

        int deleted = useCase.execute();

        assertEquals(3, deleted);
        verify(userRepository).deleteInactiveUsersCreatedBefore(any(LocalDateTime.class));
    }
}
