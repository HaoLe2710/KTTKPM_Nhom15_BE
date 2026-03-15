package fit.iuh.kttkpm_nhom15_be.payments.domain.models;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentTransaction {
    private String id;
    private String orderId;
    private PaymentProvider provider;
    private PaymentMethod method;
    private BigDecimal amount;
    private PaymentTxnStatus status;
    private String txnRef;
    private Map<String, Object> rawPayload;
}



