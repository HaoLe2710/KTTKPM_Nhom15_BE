package fit.iuh.kttkpm_nhom15_be.users.presentation.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateStaffRequest {
    @NotBlank(message = "Email khong duoc trong")
    @Email
    private String email;

    @NotBlank(message = "SDT khong duoc trong")
    private String phone;

    @NotBlank(message = "Mat khau khong duoc trong")
    private String password;

    @NotBlank(message = "Ho ten khong duoc trong")
    private String fullName;
}

