package fit.iuh.kttkpm_nhom15_be.calalog.domain.models;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Option {
    private String id;
    private String code;
    private String name;
}