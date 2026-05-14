package fit.iuh.kttkpm_nhom15_be.catalog.domain.models;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductType {
    private String id;
    private String code;
    private String name;
}