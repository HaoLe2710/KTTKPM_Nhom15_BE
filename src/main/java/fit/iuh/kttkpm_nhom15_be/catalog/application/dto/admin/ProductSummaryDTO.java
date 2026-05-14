package fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryDTO {
    private String id;
    private String typeId;
    private String name;
    private String slug;
    private BigDecimal lowestPrice;
    private long totalInitialStock;
}
