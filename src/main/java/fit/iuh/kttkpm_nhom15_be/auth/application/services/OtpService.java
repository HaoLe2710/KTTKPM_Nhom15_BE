package fit.iuh.kttkpm_nhom15_be.auth.application.services;

import fit.iuh.kttkpm_nhom15_be.auth.domain.models.Otp;
import fit.iuh.kttkpm_nhom15_be.auth.domain.repositories.OtpRepository;
import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.notification.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final OtpRepository otpRepository;
    private final EmailService emailService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendOtp(String userId, String email, String type) {
        String code = String.format("%06d", new Random().nextInt(1000000));

        Otp otp = Otp.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .email(email)
                .otpCode(code)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .isUsed(false)
                .type(type)
                .build();

        otpRepository.save(otp);

        emailService.sendOtpEmail(email, code);
    }

    @Transactional
    public boolean verifyOtp(String email, String code, String type) {
        return otpRepository.findLatestValid(email, type, LocalDateTime.now())
                .map(otp -> {

                    if (otp.isValid(code, LocalDateTime.now())) {
                        otp.setUsed(true);
                        otpRepository.save(otp);
                        return true;
                    }
                    return false;
                }).orElse(false);
    }
}