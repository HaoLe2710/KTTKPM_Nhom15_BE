package fit.iuh.kttkpm_nhom15_be.users.presentation.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "SĐT không được để trống")
    private String phone;

    @NotBlank(message = "Họ tên không được để trống")
    @JsonProperty("full_name")
    private String fullName;

    private String avatar;
}
