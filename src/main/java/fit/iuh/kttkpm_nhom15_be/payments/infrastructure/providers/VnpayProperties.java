package fit.iuh.kttkpm_nhom15_be.payments.infrastructure.providers;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vnpay")
public class VnpayProperties {
    private String tmnCode;
    private String hashSecret;
    private String payUrl;
    private String returnUrl;
    private String ipnUrl;
    private String locale = "vn";
    private String currCode = "VND";
    private String command = "pay";
    private String orderType = "other";
    private String version = "2.1.0";
}
