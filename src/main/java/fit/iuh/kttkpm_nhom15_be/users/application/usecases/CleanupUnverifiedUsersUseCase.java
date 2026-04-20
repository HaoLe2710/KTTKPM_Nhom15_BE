package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupUnverifiedUsersUseCase {
    private final UserRepository userRepository;

    @Value("${app.users.unverified-ttl-minutes:15}")
    private int unverifiedTtlMinutes;

    @Transactional
    public int execute() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(unverifiedTtlMinutes);
        int deleted = userRepository.deleteInactiveUsersCreatedBefore(cutoff);
        if (deleted > 0) {
            log.info("Deleted {} unverified users older than {} minutes", deleted, unverifiedTtlMinutes);
        }
        return deleted;
    }
}
