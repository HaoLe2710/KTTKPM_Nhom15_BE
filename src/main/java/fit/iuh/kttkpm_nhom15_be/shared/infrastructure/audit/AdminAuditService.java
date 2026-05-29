package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.audit;

import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.admin.AdminRequestContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuditService {

  private final AdminAuditLogRepository adminAuditLogRepository;

  public void log(String actionType, String resourceType, String resourceId, String summaryPayload) {
    String role = AdminRequestContextHolder.currentRole();
    adminAuditLogRepository.insert(
      actionType,
      resourceType,
      resourceId,
      role == null || role.isBlank() ? "UNKNOWN" : role,
      AdminRequestContextHolder.currentUserId(),
      summaryPayload
    );
  }
}
