package fit.iuh.kttkpm_nhom15_be.orders.application.results;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderResult {
    private String orderId;
    private String orderNo;
    
    // Nếu hệ thống tích hợp VNPAY, bạn có thể thêm field paymentUrl ở đây 
    // để Frontend tự động redirect khách sang trang quét mã QR.
     private String paymentRedirectUrl;
}