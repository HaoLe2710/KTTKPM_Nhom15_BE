package fit.iuh.kttkpm_nhom15_be.users.infrastructure.schedulers;

import fit.iuh.kttkpm_nhom15_be.users.application.usecases.CleanupUnverifiedUsersUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnverifiedUserCleanupScheduler {
    private final CleanupUnverifiedUsersUseCase cleanupUnverifiedUsersUseCase;

    @Scheduled(fixedDelayString = "${app.users.cleanup-fixed-delay-ms:60000}")
    public void cleanup() {
        cleanupUnverifiedUsersUseCase.execute();
    }
}
