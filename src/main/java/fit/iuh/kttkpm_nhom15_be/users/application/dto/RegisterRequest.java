package fit.iuh.kttkpm_nhom15_be.users.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "email khong duoc de trong")
        @Email(message = "email khong hop le")
        String email,
        @NotBlank(message = "password khong duoc de trong")
        @Size(min = 6, message = "password phai co it nhat 6 ky tu")
        String password,
        @NotBlank(message = "fullName khong duoc de trong")
        String fullName,
        String phone
) {

}
