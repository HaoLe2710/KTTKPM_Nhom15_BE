package fit.iuh.kttkpm_nhom15_be.calalog.domain.models;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Variant {
    private String id;
    private String productId;
    private String sku;
    private BigDecimal price;
    private int stockQuantity;
    private boolean isActive;
    private List<VariantOption> options;
    private List<Media> media;
}