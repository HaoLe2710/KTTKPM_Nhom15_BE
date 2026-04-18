package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.admin;

import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiForbiddenException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiUnauthorizedException;
import fit.iuh.kttkpm_nhom15_be.users.domain.models.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * FE-facing contract for the admin dashboard API.
 *
 * <p>This interceptor protects every endpoint under {@code /api/v1/admin/**}. Frontend clients should
 * treat this class as the entry point for the admin integration contract because it defines the
 * required request headers, the accepted roles, and the route groups that make up the current admin
 * dashboard surface.
 *
 * <p>Runtime API documentation:
 * <ul>
 *   <li>{@code /swagger-ui.html}</li>
 *   <li>{@code /v3/api-docs}</li>
 *   <li>{@code /v3/api-docs.yaml}</li>
 *   <li>{@code /docs/api/openapi.yaml}</li>
 *   <li>{@code /docs/api/exports/frontend/openapi.yaml}</li>
 * </ul>
 *
 * <p>Required admin headers:
 * <ul>
 *   <li>{@value #USER_ROLE_HEADER}: required, accepted values are {@code ADMIN} and {@code STAFF}</li>
 *   <li>{@value #USER_ID_HEADER}: optional for transport, but should be sent so audit and tracing
 *   can attribute admin actions to a concrete user</li>
 * </ul>
 *
 * <p>Error contract used by admin endpoints:
 * <ul>
 *   <li>Missing or blank {@code X-User-Role} returns {@code 401 Unauthorized}</li>
 *   <li>Any role other than {@code ADMIN} or {@code STAFF} returns {@code 403 Forbidden}</li>
 *   <li>Error payload shape is {@code {timestamp, status, error, message, path}}</li>
 * </ul>
 *
 * <p>Admin dashboard route groups currently available to FE:
 * <ul>
 *   <li>Catalog health: {@code GET /api/v1/admin/catalog/health}</li>
 *   <li>Products: {@code /api/v1/admin/products}, product detail, active toggle, search status,
 *   and search preview</li>
 *   <li>Variants: {@code /api/v1/admin/variants}, stock patch, active toggle, and variant media</li>
 *   <li>Options and values: {@code /api/v1/admin/options}, {@code /api/v1/admin/option-values},
 *   and {@code /api/v1/admin/options/{optionId}/values}</li>
 *   <li>Product and variant media: {@code /api/v1/admin/products/{productId}/media} and
 *   {@code /api/v1/admin/variants/{variantId}/media}; upload endpoints use
 *   {@code multipart/form-data} with the {@code file}, {@code type}, and {@code primary} fields</li>
 *   <li>Search analytics: {@code /api/v1/admin/search/top-queries},
 *   {@code /api/v1/admin/search/zero-result-queries},
 *   {@code /api/v1/admin/search/top-clicked-products}, and
 *   {@code /api/v1/admin/search/summary}</li>
 *   <li>Search projections: rebuild one, rebuild all, task list, failure list, run history, retry,
 *   and resolve endpoints under {@code /api/v1/admin/search}</li>
 *   <li>Search suggestions: {@code /api/v1/admin/search/suggestions}</li>
 *   <li>Search synonyms: {@code /api/v1/admin/search/synonym-groups} and
 *   {@code /api/v1/admin/search/synonym-terms/{termId}}</li>
 * </ul>
 *
 * <p>Shared admin list-query conventions used by multiple endpoints:
 * <ul>
 *   <li>{@code page}: default {@code 0}</li>
 *   <li>{@code size}: default {@code 20}, maximum {@code 100}</li>
 *   <li>{@code sort}: must use {@code field,asc|desc}</li>
 * </ul>
 *
 * <p>For a full admin dashboard, FE will usually combine these protected admin endpoints with the
 * reporting endpoints outside this interceptor at {@code /api/v1/analytics/dashboard} and
 * {@code /api/v1/analytics/report/pdf}.
 */
@Component
public class AdminApiRoleInterceptor implements HandlerInterceptor {

  /** Header used by FE to declare the current admin role for every /api/v1/admin request. */
  public static final String USER_ROLE_HEADER = "X-User-Role";
  /** Header used by FE to forward the acting admin user id for audit and tracing. */
  public static final String USER_ID_HEADER = "X-User-Id";
  private static final Set<String> ALLOWED_ROLES = Set.of(UserRole.ADMIN.name(), UserRole.STAFF.name());

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String roleHeader = request.getHeader(USER_ROLE_HEADER);
    if (roleHeader == null || roleHeader.isBlank()) {
      throw new ApiUnauthorizedException("Missing required header X-User-Role");
    }

    String normalizedRole = roleHeader.trim().toUpperCase();
    if (!ALLOWED_ROLES.contains(normalizedRole)) {
      throw new ApiForbiddenException("Role is not allowed to access admin APIs");
    }

    AdminRequestContextHolder.set(normalizedRole, request.getHeader(USER_ID_HEADER));
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    AdminRequestContextHolder.clear();
  }
}
