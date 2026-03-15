package fit.iuh.kttkpm_nhom15_be.users.domain.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String id;
    private String email;
    private String phone;
    private String password;
    private String fullName;
    private String avatarUrl;
    private UserRole role; // ADMIN, STAFF, CUSTOMER
    private boolean isActive;
    private List<Address> addresses;
}