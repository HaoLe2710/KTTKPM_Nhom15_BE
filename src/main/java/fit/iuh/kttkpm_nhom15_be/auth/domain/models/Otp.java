package fit.iuh.kttkpm_nhom15_be.auth.domain.models;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Otp {
    private UUID id;
    private String email;
    private String otpCode;
    private LocalDateTime expiryTime;
    private boolean isUsed;
    private String type;

    public boolean isValid(String inputCode, LocalDateTime now) {
        return !isUsed
                && expiryTime.isAfter(now)
                && this.otpCode.equals(inputCode);
    }
}
