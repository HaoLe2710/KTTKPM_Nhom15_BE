package fit.iuh.kttkpm_nhom15_be.calalog.domain.models;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionValue {
    private String id;
    private String optionId;
    private String value;
    private boolean isActive;
}