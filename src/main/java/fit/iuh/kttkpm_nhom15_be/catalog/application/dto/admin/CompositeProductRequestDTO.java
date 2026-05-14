package fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompositeProductRequestDTO {
    
    @NotBlank(message = "Type ID is fundamentally required")
    private String typeId;
    
    @NotBlank(message = "Product name cannot be blank")
    private String name;
    
    private String descriptionMd;
    
    private boolean isCustomizable;
    
    @Valid
    @NotNull(message = "At least one variant must be provided")
    private List<VariantRequestDTO> variants;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantRequestDTO {
        @NotBlank(message = "SKU is strictly required")
        private String sku;
        
        @NotNull(message = "Price is required")
        @Min(value = 0, message = "Price cannot be negative")
        private BigDecimal price;
        
        @Min(value = 0, message = "Stock quantity cannot be negative")
        private int stockQuantity;
        
        @Valid
        private List<OptionAssignmentDTO> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionAssignmentDTO {
        @NotBlank(message = "Option ID must be linked")
        private String optionId;
        
        @NotBlank(message = "Option Value ID must be targeted")
        private String valueId;
    }
}
