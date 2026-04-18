package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.admin;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiForbiddenException;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiUnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AdminApiRoleInterceptorTest {

  private final AdminApiRoleInterceptor interceptor = new AdminApiRoleInterceptor();

  @AfterEach
  void clearContext() {
    AdminRequestContextHolder.clear();
  }

  @Test
  void preHandleRejectsMissingRoleHeader() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

    assertThrows(ApiUnauthorizedException.class, () -> interceptor.preHandle(request, response, new Object()));
  }

  @Test
  void preHandleRejectsDisallowedRole() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    when(request.getHeader(AdminApiRoleInterceptor.USER_ROLE_HEADER)).thenReturn("CUSTOMER");

    assertThrows(ApiForbiddenException.class, () -> interceptor.preHandle(request, response, new Object()));
  }

  @Test
  void preHandleAllowsStaffAndStoresRequestContext() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    when(request.getHeader(AdminApiRoleInterceptor.USER_ROLE_HEADER)).thenReturn("staff");
    when(request.getHeader(AdminApiRoleInterceptor.USER_ID_HEADER)).thenReturn("user-1");

    assertDoesNotThrow(() -> interceptor.preHandle(request, response, new Object()));
  }
}
