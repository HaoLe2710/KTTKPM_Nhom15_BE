package fit.iuh.kttkpm_nhom15_be.users.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "email không được để trống")
        @Email(message = "email không hợp lệ")
        String email,
        @NotBlank(message = "password không được để trống")
        @Size(min = 6, message = "password phai co it nhat 6 ky tu")
        String password,
        @NotBlank(message = "fullName không được để trống")
        String fullName,
        String phone
) {

}
