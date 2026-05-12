package fit.iuh.kttkpm_nhom15_be.payments.presentation.requests;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SepayWebhookRequest {
    private Long id;
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String subAccount;
    private String referenceCode;
    private String code;
    private String content;
    private String transferContent;
    private String description;
    private String transferType;
    private BigDecimal transferAmount;
    private BigDecimal amount;
    private String gatewayTransactionId;
}
