package fit.iuh.kttkpm_nhom15_be.auth.presentation.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(
  @NotBlank(message = "email khong duoc de trong")
  @Email(message = "email khong hop le")
  String email,
  @NotBlank(message = "otp khong duoc de trong")
  @Pattern(regexp = "\\d{6}", message = "otp phai gom dung 6 chu so")
  String otp
) {
}
