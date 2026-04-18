package fit.iuh.kttkpm_nhom15_be.auth.infrastructure.repositories;

import fit.iuh.kttkpm_nhom15_be.auth.domain.models.Otp;
import fit.iuh.kttkpm_nhom15_be.auth.domain.repositories.OtpRepository;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.persistence.entities.OtpEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SqlOtpRepository implements OtpRepository {
    private final JpaOtpSpringRepository jpaRepository;

    @Override
    @Transactional
    public void save(Otp otp) {
        OtpEntity entity = OtpEntity.builder()
                .id(otp.getId())
                .userId(otp.getUserId())
                .email(otp.getEmail())
                .otpCode(otp.getOtpCode())
                .expiryTime(otp.getExpiryTime())
                .isUsed(otp.isUsed())
                .type(otp.getType())
                .build();
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Otp> findLatestValid(String email, String type, LocalDateTime now) {
        return jpaRepository.findFirstByEmailAndTypeAndIsUsedFalseAndExpiryTimeAfterOrderByCreatedAtDesc(email, type, now)
                .map(this::toDomain);
    }

    private Otp toDomain(OtpEntity entity) {
        return Otp.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .otpCode(entity.getOtpCode())
                .expiryTime(entity.getExpiryTime())
                .isUsed(entity.isUsed())
                .type(entity.getType())
                .email(entity.getEmail())
                .build();
    }
}