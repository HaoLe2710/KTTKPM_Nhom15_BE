package fit.iuh.kttkpm_nhom15_be.promotions.presentation.requests;

import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
public class CreatePromotionRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @NotNull
    private PromotionType type;

    @NotNull
    private Map<String, Object> config;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;

    private Integer usageLimit;
    private boolean active = true;
}
