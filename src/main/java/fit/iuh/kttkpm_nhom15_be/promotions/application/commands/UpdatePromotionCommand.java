package fit.iuh.kttkpm_nhom15_be.promotions.application.commands;

import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePromotionCommand {
    private String promotionId;
    private String code;
    private String name;
    private PromotionType type;
    private Map<String, Object> config;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer usageLimit;
    private boolean active;
}
