package fit.iuh.kttkpm_nhom15_be.agent.application.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDecision {
  private String action;
  private Map<String, Object> parameters = new HashMap<>();
}
