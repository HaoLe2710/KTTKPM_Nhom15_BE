package fit.iuh.kttkpm_nhom15_be.auth.presentation.requests;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
  @NotBlank(message = "identifier không được để trống")
  String identifier,
  @NotBlank(message = "password không được để trống")
  String password
) {
}
