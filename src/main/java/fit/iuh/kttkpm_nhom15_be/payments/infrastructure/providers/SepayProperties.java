package fit.iuh.kttkpm_nhom15_be.payments.infrastructure.providers;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sepay")
public class SepayProperties {
    private String webhookSecret;
    private String bankCode;
    private String accountNumber;
    private String accountName;
}
