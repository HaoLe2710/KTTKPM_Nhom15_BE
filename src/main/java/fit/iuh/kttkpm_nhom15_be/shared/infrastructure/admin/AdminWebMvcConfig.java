package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class AdminWebMvcConfig implements WebMvcConfigurer {

  private final AdminApiRoleInterceptor adminApiRoleInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(adminApiRoleInterceptor)
      .addPathPatterns("/api/v1/admin/**");
  }
}
