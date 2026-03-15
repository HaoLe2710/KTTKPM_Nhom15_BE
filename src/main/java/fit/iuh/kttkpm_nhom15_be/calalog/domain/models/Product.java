package fit.iuh.kttkpm_nhom15_be.calalog.domain.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String id;
    private String typeId;
    private String name;
    private String slug;
    private String descriptionMd;
    private boolean isCustomizable;
    private boolean isActive;
    private List<Variant> variants;
    private List<Media> media;
}