package fit.iuh.kttkpm_nhom15_be.catalog.domain.models;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Media {
    private String id;
    private String productId;
    private String variantId;
    private String url;
    private String publicId;
    private MediaType type;
    private boolean isPrimary;
}