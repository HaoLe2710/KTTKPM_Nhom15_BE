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
            helper.setSubject("Ma xac thuc OTP - Nhom 15 Cosmetics");
            helper.setText(buildOtpHtml(otp), true);
            mailSender.send(message);
            log.info("OTP email sent to {} with from {}", toEmail, effectiveFrom);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            log.error("Failed to send OTP email to {} with from {}: {}", toEmail, effectiveFrom, ex.getMessage());
            throw new IllegalStateException("Khong the gui email OTP", ex);
        }
    }

    private String buildOtpHtml(String otp) {
        return """
                <!DOCTYPE html>
                <html lang=\"vi\">
                <head>
                  <meta charset=\"UTF-8\" />
                  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />
                  <title>M&#227; OTP x&#225;c th&#7921;c</title>
                </head>
                <body style=\"margin:0;padding:0;background:#f5f7fb;font-family:Arial,sans-serif;color:#1f2937;\">
                  <table role=\"presentation\" width=\"100%%\" cellspacing=\"0\" cellpadding=\"0\" style=\"padding:24px 12px;\">
                    <tr>
                      <td align=\"center\">
                        <table role=\"presentation\" width=\"100%%\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:640px;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 10px 30px rgba(0,0,0,.08);\">
                          <tr>
                            <td style=\"background:linear-gradient(135deg,#0ea5e9,#2563eb);padding:28px 28px 20px 28px;color:#fff;\">
                              <h1 style=\"margin:0;font-size:22px;line-height:1.3;\">X&#225;c th&#7921;c t&#224;i kho&#7843;n c&#7911;a b&#7841;n</h1>
                              <p style=\"margin:10px 0 0 0;font-size:14px;opacity:.95;\">M&#227; OTP d&#249;ng m&#7897;t l&#7847;n &#273;&#7875; ho&#224;n t&#7845;t &#273;&#259;ng k&#253; ho&#7863;c c&#7853;p nh&#7853;t th&#244;ng tin.</p>
                            </td>
                          </tr>

                          <tr>
                            <td style=\"padding:24px 28px;\">
                              <p style=\"margin:0 0 16px 0;font-size:15px;line-height:1.6;\">Xin ch&#224;o,</p>
                              <p style=\"margin:0 0 16px 0;font-size:15px;line-height:1.6;\">B&#7841;n v&#7915;a y&#234;u c&#7847;u x&#225;c th&#7921;c tr&#234;n h&#7879; th&#7889;ng <strong>Nhom 15 Cosmetics</strong>. Vui l&#242;ng nh&#7853;p m&#227; OTP b&#234;n d&#432;&#7899;i:</p>

                              <div style=\"margin:18px 0 20px 0;padding:14px;border:1px dashed #93c5fd;border-radius:12px;background:#eff6ff;text-align:center;\">
                                <div style=\"font-size:30px;letter-spacing:8px;font-weight:700;color:#1d4ed8;\">%s</div>
                                <div style=\"margin-top:6px;font-size:13px;color:#475569;\">Hi&#7879;u l&#7921;c trong <strong>5 ph&#250;t</strong></div>
                              </div>

                              <p style=\"margin:0 0 10px 0;font-size:14px;line-height:1.6;\">N&#7871;u b&#7841;n kh&#244;ng th&#7921;c hi&#7879;n y&#234;u c&#7847;u n&#224;y, vui l&#242;ng b&#7887; qua email.</p>
                              <p style=\"margin:0;font-size:14px;line-height:1.6;\">V&#236; l&#253; do b&#7843;o m&#7853;t, kh&#244;ng chia s&#7867; m&#227; OTP cho b&#7845;t k&#7923; ai.</p>
                            </td>
                          </tr>

                          <tr>
                            <td style=\"padding:16px 28px 24px 28px;background:#f8fafc;border-top:1px solid #e5e7eb;\">
                              <p style=\"margin:0 0 6px 0;font-size:13px;color:#475569;\">Tr&#226;n tr&#7885;ng,</p>
                              <p style=\"margin:0;font-size:13px;color:#0f172a;font-weight:600;\">Nhom 15 Cosmetics</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(otp);
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
