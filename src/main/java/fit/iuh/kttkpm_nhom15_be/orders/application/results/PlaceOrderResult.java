package fit.iuh.kttkpm_nhom15_be.orders.application.results;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderResult {
    private String orderId;
    private String orderNo;
    private String paymentRedirectUrl;
    private Map<String, Object> paymentInfo;
}
