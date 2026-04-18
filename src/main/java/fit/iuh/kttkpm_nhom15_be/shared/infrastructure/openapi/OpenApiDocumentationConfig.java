package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.openapi;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiDocumentationConfig {

  @Bean
  public OpenAPI applicationOpenApi(@Value("${spring.application.name:KTTKPM_Nhom15_BE}") String applicationName,
                                    @Value("${api.docs.version:v1}") String apiVersion) {
    return new OpenAPI()
      .info(new Info()
        .title(applicationName + " API")
        .version(apiVersion)
        .description("Professional OpenAPI 3 documentation for the KTTKPM_Nhom15_BE backend.")
        .contact(new Contact()
          .name("Backend Team")
          .email("backend-team@example.local"))
        .license(new License()
          .name("Internal Use")
          .url("https://example.local/internal-use")))
      .externalDocs(new ExternalDocumentation()
        .description("Generated API artifacts for frontend and QA")
        .url("/docs/api/openapi.yaml"))
      .tags(List.of(
        tag("Admin Catalog", "Admin catalog management endpoints."),
        tag("Admin Search", "Admin search operations, metadata, and analytics endpoints."),
        tag("Analytics", "Reporting and operational metrics endpoints."),
        tag("Cart", "Shopping cart and cart line management."),
        tag("Catalog", "Catalog, product, media, and master-data endpoints."),
        tag("Chat", "Chat room and conversation endpoints."),
        tag("Orders", "Order placement and order lifecycle endpoints."),
        tag("Promotions", "Promotion management endpoints."),
        tag("Reviews", "Product review creation and moderation endpoints."),
        tag("Search", "Search, suggestions, and search projection administration endpoints."),
        tag("Users", "User management endpoints.")
      ));
  }

  @Bean
  public OpenApiCustomizer moduleTagCustomizer() {
    return openApi -> {
      if (openApi.getPaths() == null) {
        return;
      }

      openApi.getPaths().forEach((path, pathItem) ->
        pathItem.readOperations().forEach(operation -> operation.setTags(List.of(resolveTag(path))))
      );
    };
  }

  @Bean
  public GroupedOpenApi allApiGroup(OpenApiCustomizer moduleTagCustomizer) {
    return GroupedOpenApi.builder()
      .group("all")
      .pathsToMatch("/api/**")
      .addOpenApiCustomizer(moduleTagCustomizer)
      .build();
  }

  @Bean
  public GroupedOpenApi analyticsApiGroup(OpenApiCustomizer moduleTagCustomizer) {
    return moduleGroup("analytics", "fit.iuh.kttkpm_nhom15_be.analytics.presentation.controllers", moduleTagCustomizer);
  }

  @Bean
  public GroupedOpenApi cartsApiGroup(OpenApiCustomizer moduleTagCustomizer) {
    return moduleGroup("carts", "fit.iuh.kttkpm_nhom15_be.carts.presentation.controllers", moduleTagCustomizer);
  }

  @Bean
  public GroupedOpenApi catalogApiGroup(OpenApiCustomizer moduleTagCustomizer) {
    return moduleGroup("catalog", "fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers", moduleTagCustomizer);
  }

  @Bean
  public GroupedOpenApi chatApiGroup(OpenApiCustomizer moduleTagCustomizer) {
    return moduleGroup("chat", "fit.iuh.kttkpm_nhom15_be.chat.presentation.controllers", moduleTagCustomizer);
  }

  @Bean
  public GroupedOpenApi ordersApiGroup(OpenApiCustomizer moduleTagCustomizer) {
    return moduleGroup("orders", "fit.iuh.kttkpm_nhom15_be.orders.presentation.controllers", moduleTagCustomizer);
  }

  @Bean
  public GroupedOpenApi promotionsApiGroup(OpenApiCustomizer moduleTagCustomizer) {
    return moduleGroup("promotions", "fit.iuh.kttkpm_nhom15_be.promotions.presentation.controllers", moduleTagCustomizer);
  }

  @Bean
  public GroupedOpenApi reviewsApiGroup(OpenApiCustomizer moduleTagCustomizer) {
    return moduleGroup("reviews", "fit.iuh.kttkpm_nhom15_be.reviews.presentation.controllers", moduleTagCustomizer);
  }

  @Bean
  public GroupedOpenApi searchApiGroup(OpenApiCustomizer moduleTagCustomizer) {
    return moduleGroup("search", "fit.iuh.kttkpm_nhom15_be.search.presentation.controllers", moduleTagCustomizer);
  }

  @Bean
  public GroupedOpenApi usersApiGroup(OpenApiCustomizer moduleTagCustomizer) {
    return moduleGroup("users", "fit.iuh.kttkpm_nhom15_be.users.presentation.controllers", moduleTagCustomizer);
  }

  private GroupedOpenApi moduleGroup(String groupName, String packageToScan, OpenApiCustomizer moduleTagCustomizer) {
    return GroupedOpenApi.builder()
      .group(groupName)
      .packagesToScan(packageToScan)
      .pathsToMatch("/api/**")
      .addOpenApiCustomizer(moduleTagCustomizer)
      .build();
  }

  private Tag tag(String name, String description) {
    return new Tag().name(name).description(description);
  }

  private String resolveTag(String path) {
    if (path.startsWith("/api/v1/admin/search")) {
      return "Admin Search";
    }
    if (path.startsWith("/api/v1/admin")) {
      return "Admin Catalog";
    }
    if (path.startsWith("/api/v1/analytics")) {
      return "Analytics";
    }
    if (path.startsWith("/api/v1/carts")) {
      return "Cart";
    }
    if (path.startsWith("/api/v1/chat")) {
      return "Chat";
    }
    if (path.startsWith("/api/v1/orders")) {
      return "Orders";
    }
    if (path.startsWith("/api/v1/promotions")) {
      return "Promotions";
    }
    if (path.startsWith("/api/v1/reviews")) {
      return "Reviews";
    }
    if (path.startsWith("/api/v1/search") || path.startsWith("/api/v1/products/search")) {
      return "Search";
    }
    if (path.startsWith("/api/v1/users")) {
      return "Users";
    }
    if (path.startsWith("/api/v1/products") || path.startsWith("/api/v1/product-types") || path.startsWith("/api/v1/options")) {
      return "Catalog";
    }
    return "Catalog";
  }
}
