package fit.iuh.kttkpm_nhom15_be.agent.application.dto;

import java.util.Map;

public record AgentResponse(
  String reply,
  String action,
  Map<String, Object> parameters,
  Object toolResult
) {
  public static AgentResponse fromResult(AgentResult result) {
    return new AgentResponse(
      result.reply(),
      result.action(),
      result.parameters(),
      result.toolResult()
    );
  }
}
