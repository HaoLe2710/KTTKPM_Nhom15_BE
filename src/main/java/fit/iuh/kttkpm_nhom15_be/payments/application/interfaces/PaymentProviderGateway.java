package fit.iuh.kttkpm_nhom15_be.payments.application.interfaces;

import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.payments.application.dto.PaymentInitializationResult;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod;
import fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentTransaction;

public interface PaymentProviderGateway {
    PaymentMethod supportedMethod();

    PaymentInitializationResult initialize(Order order, PaymentTransaction paymentTransaction, String clientIp);

    default boolean supports(PaymentMethod method) {
        return supportedMethod() == method;
    }
}
