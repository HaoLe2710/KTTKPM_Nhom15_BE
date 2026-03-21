package fit.iuh.kttkpm_nhom15_be.orders.application.results;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelOrderResult {
  private String orderId;
  private String orderNo;
  private String status;
}
