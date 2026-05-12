package fit.iuh.kttkpm_nhom15_be.auth.presentation.requests;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
  @NotBlank(message = "identifier khong duoc de trong")
  String identifier,
  @NotBlank(message = "password khong duoc de trong")
  String password
) {
}
