package fit.iuh.kttkpm_nhom15_be.auth.infrastructure.repositories;

import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.persistence.entities.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaOtpSpringRepository extends JpaRepository<OtpEntity, UUID> {

    Optional<OtpEntity> findFirstByEmailAndTypeAndIsUsedFalseAndExpiryTimeAfterOrderByCreatedAtDesc(
            String email,
            String otpCode,
            LocalDateTime now
    );
}