package fit.iuh.kttkpm_nhom15_be.orders.application.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantSnapshot {
    private String variantId;
    private String sku;
    private String productName;
    private String imageUrl;
    private BigDecimal currentPrice;
    
    // Map chứa các thuộc tính động. Ví dụ: {"Màu sắc": "Đỏ", "Dung tích": "50ml"}
    private Map<String, Object> attributes; 
}