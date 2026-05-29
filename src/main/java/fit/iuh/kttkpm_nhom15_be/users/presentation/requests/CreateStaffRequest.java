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
    @NotBlank(message = "Email không được trống")
    @Email
    private String email;

    @NotBlank(message = "SĐT không được trống")
    private String phone;

    @NotBlank(message = "Mật khẩu không được trống")
    private String password;

    @NotBlank(message = "Họ tên không được trống")
    private String fullName;
}

