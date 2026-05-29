package fit.iuh.kttkpm_nhom15_be.auth.presentation.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(
  @NotBlank(message = "email không được để trống")
  @Email(message = "email không hợp lệ")
  String email,
  @NotBlank(message = "otp không được để trống")
  @Pattern(regexp = "\\d{6}", message = "otp phai gom dung 6 chu so")
  String otp
) {
}
