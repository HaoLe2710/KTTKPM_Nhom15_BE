package fit.iuh.kttkpm_nhom15_be.users.domain.models;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String id;
    private String userId;
    private String fullName;
    private String phone;
    private String address;
    private String city;
    private String district;
    private String ward;
    private boolean isDefault;
}