package fit.iuh.kttkpm_nhom15_be.payments.infrastructure.providers;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.payments.application.dto.PaymentInitializationResult;
import fit.iuh.kttkpm_nhom15_be.payments.application.interfaces.PaymentProviderGateway;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentProvider;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTransaction;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTxnStatus;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CodPaymentProvider implements PaymentProviderGateway {

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.COD;
    }

    @Override
    public PaymentInitializationResult initialize(Order order, PaymentTransaction paymentTransaction, String clientIp) {
        Map<String, Object> paymentInfo = new LinkedHashMap<>();
        paymentInfo.put("instructions", "Thanh toán khi nhận hàng.");
        paymentInfo.put("paymentMethod", PaymentMethod.COD.name());

        Map<String, Object> rawPayload = new LinkedHashMap<>();
        rawPayload.put("paymentInfo", paymentInfo);

        return new PaymentInitializationResult(
            PaymentProvider.COD,
            PaymentTxnStatus.PENDING,
            null,
            paymentInfo,
            rawPayload
        );
    }
}
