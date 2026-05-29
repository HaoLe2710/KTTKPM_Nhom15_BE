package fit.iuh.kttkpm_nhom15_be.agent.application.dto;

import java.util.Map;

public record AgentResult(
  String reply,
  String action,
  Map<String, Object> parameters,
  Object toolResult
) {}
