package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.notification;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from-address}")
    private String fromAddress;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.mail.reply-to}")
    private String replyTo;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    public void sendOtpEmail(String toEmail, String otp) {
        var message = mailSender.createMimeMessage();
        String effectiveFrom = resolveFromAddress();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(effectiveFrom, fromName);
            helper.setReplyTo(replyTo);
            helper.setTo(toEmail);
            helper.setSubject("Ma xac thuc OTP - Nhom 15");
            helper.setText("Ma OTP cua ban la: " + otp + "\nHieu luc trong 5 phut.", false);
            mailSender.send(message);
            log.info("OTP email sent to {} with from {}", toEmail, effectiveFrom);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            log.error("Failed to send OTP email to {} with from {}: {}", toEmail, effectiveFrom, ex.getMessage());
            throw new IllegalStateException("Khong the gui email OTP", ex);
        }
    }

    private String resolveFromAddress() {
        if (fromAddress == null || fromAddress.isBlank() || fromAddress.contains("your-domain.com")) {
            if (smtpUsername != null && !smtpUsername.isBlank()) {
                log.warn("app.mail.from-address is not configured/verified, fallback to SMTP username: {}", smtpUsername);
                return smtpUsername;
            }
        }
        return fromAddress;
    }
}
