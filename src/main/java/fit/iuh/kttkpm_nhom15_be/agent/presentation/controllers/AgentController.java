package fit.iuh.kttkpm_nhom15_be.agent.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.agent.application.dto.AgentRequest;
import fit.iuh.kttkpm_nhom15_be.agent.application.dto.AgentResponse;
import fit.iuh.kttkpm_nhom15_be.agent.application.services.AgentService;
import fit.iuh.kttkpm_nhom15_be.shared.presentation.advice.SkipSuccessEnvelope;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SkipSuccessEnvelope
@RequiredArgsConstructor
@RequestMapping("/api/agent")
public class AgentController {

  private final AgentService agentService;

  @PostMapping("/message")
  public ResponseEntity<AgentResponse> handleMessage(@Valid @RequestBody AgentRequest request) {
    return ResponseEntity.ok(AgentResponse.fromResult(
      agentService.handleMessage(request.userId(), request.message())
    ));
  }
}
