package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtpEntity {
    @Id
    private UUID id;
    private String email;
    private String otpCode;
    private LocalDateTime expiryTime;
    private boolean isUsed;
    private String type;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }
}
