package fit.iuh.kttkpm_nhom15_be.agent.application.dto;

import jakarta.validation.constraints.NotBlank;

public record AgentRequest(
  String userId,
  @NotBlank(message = "message không được để trống")
  String message
) {}
