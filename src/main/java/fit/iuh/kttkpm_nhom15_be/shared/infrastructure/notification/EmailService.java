package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.notification;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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

    public record OrderConfirmationEmail(
            String toEmail,
            String recipientName,
            String recipientPhone,
            String orderNo,
            String shippingAddress,
            String paymentMethod,
            BigDecimal subtotalAmount,
            BigDecimal discountAmount,
            BigDecimal shippingFee,
            BigDecimal totalAmount,
            List<OrderConfirmationItem> items
    ) {}

    public record OrderConfirmationItem(
            String name,
            String sku,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}

    public record OrderCancellationEmail(
            String toEmail,
            String recipientName,
            String recipientPhone,
            String orderNo,
            String shippingAddress,
            String paymentMethod,
            BigDecimal totalAmount,
            String reason
    ) {}

    public void sendOtpEmail(String toEmail, String otp) {
        var message = mailSender.createMimeMessage();
        String effectiveFrom = resolveFromAddress();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(effectiveFrom, fromName);
            helper.setReplyTo(replyTo);
            helper.setTo(toEmail);
            helper.setSubject("Mã xác thực OTP - Nhóm 15 Cosmetics");
            helper.setText(buildOtpHtml(otp), true);
            mailSender.send(message);
            log.info("OTP email sent to {} with from {}", toEmail, effectiveFrom);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            log.error("Failed to send OTP email to {} with from {}: {}", toEmail, effectiveFrom, ex.getMessage());
            throw new IllegalStateException("Không thể gửi email OTP", ex);
        }
    }

    public void sendOrderConfirmationEmail(OrderConfirmationEmail email) {
        if (email == null || email.toEmail() == null || email.toEmail().isBlank()) {
            log.warn("Skip order confirmation email because recipient email is blank");
            return;
        }

        var message = mailSender.createMimeMessage();
        String effectiveFrom = resolveFromAddress();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(effectiveFrom, fromName);
            helper.setReplyTo(replyTo);
            helper.setTo(email.toEmail().trim());
            helper.setSubject("Xac nhan don hang " + safeText(email.orderNo()));
            helper.setText(buildOrderConfirmationHtml(email), true);
            mailSender.send(message);
            log.info("Order confirmation email sent to {} for order {}", email.toEmail(), email.orderNo());
        } catch (MessagingException | UnsupportedEncodingException ex) {
            log.error("Failed to send order confirmation email to {} for order {}: {}", email.toEmail(), email.orderNo(), ex.getMessage());
            throw new IllegalStateException("Khong the gui email xac nhan don hang", ex);
        }
    }

    public void sendOrderCancellationEmail(OrderCancellationEmail email) {
        if (email == null || email.toEmail() == null || email.toEmail().isBlank()) {
            log.warn("Skip order cancellation email because recipient email is blank");
            return;
        }

        var message = mailSender.createMimeMessage();
        String effectiveFrom = resolveFromAddress();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(effectiveFrom, fromName);
            helper.setReplyTo(replyTo);
            helper.setTo(email.toEmail().trim());
            helper.setSubject("Thong bao huy don hang " + safeText(email.orderNo()));
            helper.setText(buildOrderCancellationHtml(email), true);
            mailSender.send(message);
            log.info("Order cancellation email sent to {} for order {}", email.toEmail(), email.orderNo());
        } catch (MessagingException | UnsupportedEncodingException ex) {
            log.error("Failed to send order cancellation email to {} for order {}: {}", email.toEmail(), email.orderNo(), ex.getMessage());
            throw new IllegalStateException("Khong the gui email thong bao huy don hang", ex);
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

    private String buildOrderConfirmationHtml(OrderConfirmationEmail email) {
        String orderNo = escapeHtml(safeText(email.orderNo()));
        String recipientName = escapeHtml(defaultText(email.recipientName(), "Quy khach"));
        String recipientPhone = escapeHtml(safeText(email.recipientPhone()));
        String shippingAddress = escapeHtml(safeText(email.shippingAddress()));
        String paymentMethod = escapeHtml(safeText(email.paymentMethod()));
        String itemRows = buildOrderItemRows(email.items());

        return """
                <!DOCTYPE html>
                <html lang=\"vi\">
                <head>
                  <meta charset=\"UTF-8\" />
                  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />
                  <title>Xac nhan don hang</title>
                </head>
                <body style=\"margin:0;padding:0;background:#f5f7fb;font-family:Arial,sans-serif;color:#1f2937;\">
                  <table role=\"presentation\" width=\"100%%\" cellspacing=\"0\" cellpadding=\"0\" style=\"padding:24px 12px;\">
                    <tr>
                      <td align=\"center\">
                        <table role=\"presentation\" width=\"100%%\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:720px;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 10px 30px rgba(0,0,0,.08);\">
                          <tr>
                            <td style=\"background:linear-gradient(135deg,#111827,#374151);padding:28px;color:#fff;\">
                              <h1 style=\"margin:0;font-size:22px;line-height:1.3;\">Don hang cua ban da duoc ghi nhan</h1>
                              <p style=\"margin:10px 0 0 0;font-size:14px;opacity:.95;\">Ma don hang: <strong>%s</strong></p>
                            </td>
                          </tr>
                          <tr>
                            <td style=\"padding:24px 28px;\">
                              <p style=\"margin:0 0 14px 0;font-size:15px;line-height:1.6;\">Xin chao <strong>%s</strong>,</p>
                              <p style=\"margin:0 0 18px 0;font-size:15px;line-height:1.6;\">Cam on ban da dat hang tai <strong>Nhom 15 Cosmetics</strong>. Chung toi da nhan duoc don hang va se xu ly trong thoi gian som nhat.</p>

                              <table role=\"presentation\" width=\"100%%\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin:0 0 20px 0;border:1px solid #e5e7eb;border-radius:12px;overflow:hidden;\">
                                <tr>
                                  <td style=\"padding:12px 14px;background:#f8fafc;font-size:13px;color:#64748b;width:34%%;\">Nguoi nhan</td>
                                  <td style=\"padding:12px 14px;font-size:14px;color:#111827;\">%s</td>
                                </tr>
                                <tr>
                                  <td style=\"padding:12px 14px;background:#f8fafc;font-size:13px;color:#64748b;\">So dien thoai</td>
                                  <td style=\"padding:12px 14px;font-size:14px;color:#111827;\">%s</td>
                                </tr>
                                <tr>
                                  <td style=\"padding:12px 14px;background:#f8fafc;font-size:13px;color:#64748b;\">Dia chi giao hang</td>
                                  <td style=\"padding:12px 14px;font-size:14px;color:#111827;line-height:1.5;\">%s</td>
                                </tr>
                                <tr>
                                  <td style=\"padding:12px 14px;background:#f8fafc;font-size:13px;color:#64748b;\">Thanh toan</td>
                                  <td style=\"padding:12px 14px;font-size:14px;color:#111827;\">%s</td>
                                </tr>
                              </table>

                              <table role=\"presentation\" width=\"100%%\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse:collapse;margin:0 0 20px 0;\">
                                <thead>
                                  <tr>
                                    <th align=\"left\" style=\"border-bottom:1px solid #e5e7eb;padding:10px 8px;font-size:12px;color:#64748b;text-transform:uppercase;\">San pham</th>
                                    <th align=\"center\" style=\"border-bottom:1px solid #e5e7eb;padding:10px 8px;font-size:12px;color:#64748b;text-transform:uppercase;\">SL</th>
                                    <th align=\"right\" style=\"border-bottom:1px solid #e5e7eb;padding:10px 8px;font-size:12px;color:#64748b;text-transform:uppercase;\">Don gia</th>
                                    <th align=\"right\" style=\"border-bottom:1px solid #e5e7eb;padding:10px 8px;font-size:12px;color:#64748b;text-transform:uppercase;\">Thanh tien</th>
                                  </tr>
                                </thead>
                                <tbody>
                                  %s
                                </tbody>
                              </table>

                              <table role=\"presentation\" width=\"100%%\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin-left:auto;max-width:360px;\">
                                <tr>
                                  <td style=\"padding:6px 0;font-size:14px;color:#64748b;\">Tam tinh</td>
                                  <td align=\"right\" style=\"padding:6px 0;font-size:14px;color:#111827;\">%s</td>
                                </tr>
                                <tr>
                                  <td style=\"padding:6px 0;font-size:14px;color:#64748b;\">Giam gia</td>
                                  <td align=\"right\" style=\"padding:6px 0;font-size:14px;color:#111827;\">-%s</td>
                                </tr>
                                <tr>
                                  <td style=\"padding:6px 0;font-size:14px;color:#64748b;\">Phi van chuyen</td>
                                  <td align=\"right\" style=\"padding:6px 0;font-size:14px;color:#111827;\">%s</td>
                                </tr>
                                <tr>
                                  <td style=\"padding:12px 0 0 0;border-top:1px solid #e5e7eb;font-size:16px;font-weight:700;color:#111827;\">Tong cong</td>
                                  <td align=\"right\" style=\"padding:12px 0 0 0;border-top:1px solid #e5e7eb;font-size:18px;font-weight:700;color:#111827;\">%s</td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style=\"padding:16px 28px 24px 28px;background:#f8fafc;border-top:1px solid #e5e7eb;\">
                              <p style=\"margin:0 0 6px 0;font-size:13px;color:#475569;\">Tran trong,</p>
                              <p style=\"margin:0;font-size:13px;color:#0f172a;font-weight:600;\">Nhom 15 Cosmetics</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                orderNo,
                recipientName,
                recipientName,
                recipientPhone,
                shippingAddress,
                paymentMethod,
                itemRows,
                formatMoney(email.subtotalAmount()),
                formatMoney(email.discountAmount()),
                formatMoney(email.shippingFee()),
                formatMoney(email.totalAmount())
        );
    }

    private String buildOrderCancellationHtml(OrderCancellationEmail email) {
        String orderNo = escapeHtml(safeText(email.orderNo()));
        String recipientName = escapeHtml(defaultText(email.recipientName(), "Quy khach"));
        String recipientPhone = escapeHtml(safeText(email.recipientPhone()));
        String shippingAddress = escapeHtml(safeText(email.shippingAddress()));
        String paymentMethod = escapeHtml(safeText(email.paymentMethod()));
        String reason = escapeHtml(defaultText(email.reason(), "Khach hang huy don khi chua thanh toan"));

        return """
                <!DOCTYPE html>
                <html lang=\"vi\">
                <head>
                  <meta charset=\"UTF-8\" />
                  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />
                  <title>Thong bao huy don hang</title>
                </head>
                <body style=\"margin:0;padding:0;background:#f5f7fb;font-family:Arial,sans-serif;color:#1f2937;\">
                  <table role=\"presentation\" width=\"100%%\" cellspacing=\"0\" cellpadding=\"0\" style=\"padding:24px 12px;\">
                    <tr>
                      <td align=\"center\">
                        <table role=\"presentation\" width=\"100%%\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:720px;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 10px 30px rgba(0,0,0,.08);\">
                          <tr>
                            <td style=\"background:linear-gradient(135deg,#7f1d1d,#dc2626);padding:28px;color:#fff;\">
                              <h1 style=\"margin:0;font-size:22px;line-height:1.3;\">Don hang cua ban da duoc huy</h1>
                              <p style=\"margin:10px 0 0 0;font-size:14px;opacity:.95;\">Ma don hang: <strong>%s</strong></p>
                            </td>
                          </tr>
                          <tr>
                            <td style=\"padding:24px 28px;\">
                              <p style=\"margin:0 0 14px 0;font-size:15px;line-height:1.6;\">Xin chao <strong>%s</strong>,</p>
                              <p style=\"margin:0 0 18px 0;font-size:15px;line-height:1.6;\">Nhom 15 Cosmetics da ghi nhan yeu cau huy don hang khi don hang chua thanh toan.</p>

                              <table role=\"presentation\" width=\"100%%\" cellspacing=\"0\" cellpadding=\"0\" style=\"margin:0 0 20px 0;border:1px solid #e5e7eb;border-radius:12px;overflow:hidden;\">
                                <tr>
                                  <td style=\"padding:12px 14px;background:#f8fafc;font-size:13px;color:#64748b;width:34%%;\">Nguoi nhan</td>
                                  <td style=\"padding:12px 14px;font-size:14px;color:#111827;\">%s</td>
                                </tr>
                                <tr>
                                  <td style=\"padding:12px 14px;background:#f8fafc;font-size:13px;color:#64748b;\">So dien thoai</td>
                                  <td style=\"padding:12px 14px;font-size:14px;color:#111827;\">%s</td>
                                </tr>
                                <tr>
                                  <td style=\"padding:12px 14px;background:#f8fafc;font-size:13px;color:#64748b;\">Dia chi giao hang</td>
                                  <td style=\"padding:12px 14px;font-size:14px;color:#111827;line-height:1.5;\">%s</td>
                                </tr>
                                <tr>
                                  <td style=\"padding:12px 14px;background:#f8fafc;font-size:13px;color:#64748b;\">Thanh toan</td>
                                  <td style=\"padding:12px 14px;font-size:14px;color:#111827;\">%s</td>
                                </tr>
                                <tr>
                                  <td style=\"padding:12px 14px;background:#f8fafc;font-size:13px;color:#64748b;\">Ly do huy</td>
                                  <td style=\"padding:12px 14px;font-size:14px;color:#111827;line-height:1.5;\">%s</td>
                                </tr>
                                <tr>
                                  <td style=\"padding:12px 14px;background:#f8fafc;font-size:13px;color:#64748b;\">Tong tien</td>
                                  <td style=\"padding:12px 14px;font-size:16px;font-weight:700;color:#111827;\">%s</td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style=\"padding:16px 28px 24px 28px;background:#f8fafc;border-top:1px solid #e5e7eb;\">
                              <p style=\"margin:0 0 6px 0;font-size:13px;color:#475569;\">Tran trong,</p>
                              <p style=\"margin:0;font-size:13px;color:#0f172a;font-weight:600;\">Nhom 15 Cosmetics</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                orderNo,
                recipientName,
                recipientName,
                recipientPhone,
                shippingAddress,
                paymentMethod,
                reason,
                formatMoney(email.totalAmount())
        );
    }

    private String buildOrderItemRows(List<OrderConfirmationItem> items) {
        if (items == null || items.isEmpty()) {
            return """
                    <tr>
                      <td colspan=\"4\" style=\"padding:14px 8px;font-size:14px;color:#64748b;text-align:center;\">Khong co du lieu san pham.</td>
                    </tr>
                    """;
        }

        return items.stream()
                .map(item -> """
                        <tr>
                          <td style=\"border-bottom:1px solid #f1f5f9;padding:12px 8px;font-size:14px;color:#111827;\">
                            <div style=\"font-weight:600;\">%s</div>
                            <div style=\"margin-top:4px;font-size:12px;color:#64748b;\">%s</div>
                          </td>
                          <td align=\"center\" style=\"border-bottom:1px solid #f1f5f9;padding:12px 8px;font-size:14px;color:#111827;\">%d</td>
                          <td align=\"right\" style=\"border-bottom:1px solid #f1f5f9;padding:12px 8px;font-size:14px;color:#111827;\">%s</td>
                          <td align=\"right\" style=\"border-bottom:1px solid #f1f5f9;padding:12px 8px;font-size:14px;color:#111827;font-weight:600;\">%s</td>
                        </tr>
                        """.formatted(
                        escapeHtml(defaultText(item.name(), "San pham")),
                        escapeHtml(defaultText(item.sku(), "")),
                        item.quantity(),
                        formatMoney(item.unitPrice()),
                        formatMoney(item.lineTotal())
                ))
                .reduce("", String::concat);
    }

    private String formatMoney(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
        return formatter.format(amount == null ? BigDecimal.ZERO : amount);
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String safeText(String value) {
        return defaultText(value, "");
    }

    private String escapeHtml(String value) {
        return safeText(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
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
