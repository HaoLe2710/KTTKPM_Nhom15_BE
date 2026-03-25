package fit.iuh.kttkpm_nhom15_be.users.presentation.requests;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "Email không được trống") @Email private String email;
    @NotBlank(message = "SĐT không được trống") private String phone;
    @NotBlank(message = "Mật khẩu không được trống") private String password;
    @NotBlank(message = "Họ tên không được trống") private String fullName;
    @NotNull(message = "Role không được trống") private UserRole role;
}
