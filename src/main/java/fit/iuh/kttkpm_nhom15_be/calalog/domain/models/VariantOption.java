package fit.iuh.kttkpm_nhom15_be.calalog.domain.models;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantOption {
    private String id;
    private String variantId;
    private String optionValueId;
}