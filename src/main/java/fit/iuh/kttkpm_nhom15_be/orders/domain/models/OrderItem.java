package fit.iuh.kttkpm_nhom15_be.orders.domain.models;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private String id;
    private String variantId;
    private String sku;
    private String name;
    private Map<String, Object> optionsSnapshot;
    private String imageUrl;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}