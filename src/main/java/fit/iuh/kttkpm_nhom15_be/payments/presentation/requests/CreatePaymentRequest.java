package fit.iuh.kttkpm_nhom15_be.payments.presentation.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePaymentRequest {
    @NotBlank(message = "orderId must not be blank")
    private String orderId;
}
