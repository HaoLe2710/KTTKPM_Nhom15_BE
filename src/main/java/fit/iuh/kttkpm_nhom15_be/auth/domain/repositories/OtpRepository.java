package fit.iuh.kttkpm_nhom15_be.auth.domain.repositories;

import fit.iuh.kttkpm_nhom15_be.auth.domain.models.Otp;
import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpRepository {
    void save(Otp otp);
    Optional<Otp> findLatestValid(String email, String type, LocalDateTime now);
}