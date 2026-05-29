package fit.iuh.kttkpm_nhom15_be.promotions.presentation.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ValidatePromotionRequest {
    @NotBlank
    private String promotionCode;

    @NotNull
    @PositiveOrZero
    private BigDecimal subtotal;

    @NotNull
    @Valid
    private List<Item> items;

    @Getter
    @Setter
    public static class Item {
        @NotBlank
        private String variantId;

        @Positive
        private int quantity;

        @NotNull
        @PositiveOrZero
        private BigDecimal unitPrice;

        @NotNull
        @PositiveOrZero
        private BigDecimal lineTotal;
    }
}
