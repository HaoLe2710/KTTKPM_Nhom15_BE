package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.openapi;

import fit.iuh.kttkpm_nhom15_be.shared.presentation.advice.ApiErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiDocumentationConfig {

  private static final Set<String> RAW_SUCCESS_RESPONSE_PATHS = Set.of(
    "/api/v1/analytics/report/pdf",
    "/api/v1/payments/vnpay-return",
    "/api/v1/payments/vnpay-ipn",
    "/api/v1/payments/sepay-webhook"
  );

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
        tag("Payments", "Payment creation, callbacks, webhooks, and payment-status endpoints."),
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
  public OpenApiCustomizer standardizedErrorResponseCustomizer() {
    return openApi -> {
      if (openApi.getComponents() == null) {
        openApi.setComponents(new Components());
      }

      Map<String, Schema> schemas = ModelConverters.getInstance().read(ApiErrorResponse.class);
      schemas.forEach((name, schema) -> openApi.getComponents().addSchemas(name, schema));

      if (openApi.getPaths() == null) {
        return;
      }

      openApi.getPaths().forEach((path, pathItem) ->
        pathItem.readOperations().forEach(operation -> {
          ApiResponses responses = operation.getResponses();
          addErrorResponse(responses, "400", "Bad Request");
          addErrorResponse(responses, "401", "Unauthorized");
          addErrorResponse(responses, "403", "Forbidden");
          addErrorResponse(responses, "404", "Not Found");
          addErrorResponse(responses, "409", "Conflict");
          addErrorResponse(responses, "500", "Internal Server Error");
        })
      );
    };
  }

  @Bean
  public OpenApiCustomizer standardizedSuccessResponseCustomizer() {
    return openApi -> {
      if (openApi.getPaths() == null) {
        return;
      }

      openApi.getPaths().forEach((path, pathItem) -> {
        if (RAW_SUCCESS_RESPONSE_PATHS.contains(path)) {
          return;
        }

        pathItem.readOperationsMap().forEach((httpMethod, operation) ->
          operation.getResponses().forEach((statusCode, apiResponse) ->
            wrapSuccessResponse(path, httpMethod, statusCode, apiResponse)
          )
        );
      });
    };
  }

  @Bean
  public GroupedOpenApi allApiGroup(OpenApiCustomizer moduleTagCustomizer,
                                    OpenApiCustomizer standardizedErrorResponseCustomizer,
                                    OpenApiCustomizer standardizedSuccessResponseCustomizer) {
    return GroupedOpenApi.builder()
      .group("all")
      .pathsToMatch("/api/**")
      .addOpenApiCustomizer(moduleTagCustomizer)
      .addOpenApiCustomizer(standardizedErrorResponseCustomizer)
      .addOpenApiCustomizer(standardizedSuccessResponseCustomizer)
      .build();
  }

  @Bean
  public GroupedOpenApi analyticsApiGroup(OpenApiCustomizer moduleTagCustomizer,
                                          OpenApiCustomizer standardizedErrorResponseCustomizer,
                                          OpenApiCustomizer standardizedSuccessResponseCustomizer) {
    return moduleGroup(
      "analytics",
      "fit.iuh.kttkpm_nhom15_be.analytics.presentation.controllers",
      moduleTagCustomizer,
      standardizedErrorResponseCustomizer,
      standardizedSuccessResponseCustomizer
    );
  }

  @Bean
  public GroupedOpenApi cartsApiGroup(OpenApiCustomizer moduleTagCustomizer,
                                      OpenApiCustomizer standardizedErrorResponseCustomizer,
                                      OpenApiCustomizer standardizedSuccessResponseCustomizer) {
    return moduleGroup(
      "carts",
      "fit.iuh.kttkpm_nhom15_be.carts.presentation.controllers",
      moduleTagCustomizer,
      standardizedErrorResponseCustomizer,
      standardizedSuccessResponseCustomizer
    );
  }

  @Bean
  public GroupedOpenApi catalogApiGroup(OpenApiCustomizer moduleTagCustomizer,
                                        OpenApiCustomizer standardizedErrorResponseCustomizer,
                                        OpenApiCustomizer standardizedSuccessResponseCustomizer) {
    return moduleGroup(
      "catalog",
      "fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers",
      moduleTagCustomizer,
      standardizedErrorResponseCustomizer,
      standardizedSuccessResponseCustomizer
    );
  }

  @Bean
  public GroupedOpenApi chatApiGroup(OpenApiCustomizer moduleTagCustomizer,
                                     OpenApiCustomizer standardizedErrorResponseCustomizer,
                                     OpenApiCustomizer standardizedSuccessResponseCustomizer) {
    return moduleGroup(
      "chat",
      "fit.iuh.kttkpm_nhom15_be.chat.presentation.controllers",
      moduleTagCustomizer,
      standardizedErrorResponseCustomizer,
      standardizedSuccessResponseCustomizer
    );
  }

  @Bean
  public GroupedOpenApi ordersApiGroup(OpenApiCustomizer moduleTagCustomizer,
                                       OpenApiCustomizer standardizedErrorResponseCustomizer,
                                       OpenApiCustomizer standardizedSuccessResponseCustomizer) {
    return moduleGroup(
      "orders",
      "fit.iuh.kttkpm_nhom15_be.orders.presentation.controllers",
      moduleTagCustomizer,
      standardizedErrorResponseCustomizer,
      standardizedSuccessResponseCustomizer
    );
  }

  @Bean
  public GroupedOpenApi paymentsApiGroup(OpenApiCustomizer moduleTagCustomizer,
                                         OpenApiCustomizer standardizedErrorResponseCustomizer,
                                         OpenApiCustomizer standardizedSuccessResponseCustomizer) {
    return moduleGroup(
      "payments",
      "fit.iuh.kttkpm_nhom15_be.payments.presentation.controllers",
      moduleTagCustomizer,
      standardizedErrorResponseCustomizer,
      standardizedSuccessResponseCustomizer
    );
  }

  @Bean
  public GroupedOpenApi promotionsApiGroup(OpenApiCustomizer moduleTagCustomizer,
                                           OpenApiCustomizer standardizedErrorResponseCustomizer,
                                           OpenApiCustomizer standardizedSuccessResponseCustomizer) {
    return moduleGroup(
      "promotions",
      "fit.iuh.kttkpm_nhom15_be.promotions.presentation.controllers",
      moduleTagCustomizer,
      standardizedErrorResponseCustomizer,
      standardizedSuccessResponseCustomizer
    );
  }

  @Bean
  public GroupedOpenApi reviewsApiGroup(OpenApiCustomizer moduleTagCustomizer,
                                        OpenApiCustomizer standardizedErrorResponseCustomizer,
                                        OpenApiCustomizer standardizedSuccessResponseCustomizer) {
    return moduleGroup(
      "reviews",
      "fit.iuh.kttkpm_nhom15_be.reviews.presentation.controllers",
      moduleTagCustomizer,
      standardizedErrorResponseCustomizer,
      standardizedSuccessResponseCustomizer
    );
  }

  @Bean
  public GroupedOpenApi searchApiGroup(OpenApiCustomizer moduleTagCustomizer,
                                       OpenApiCustomizer standardizedErrorResponseCustomizer,
                                       OpenApiCustomizer standardizedSuccessResponseCustomizer) {
    return moduleGroup(
      "search",
      "fit.iuh.kttkpm_nhom15_be.search.presentation.controllers",
      moduleTagCustomizer,
      standardizedErrorResponseCustomizer,
      standardizedSuccessResponseCustomizer
    );
  }

  @Bean
  public GroupedOpenApi usersApiGroup(OpenApiCustomizer moduleTagCustomizer,
                                      OpenApiCustomizer standardizedErrorResponseCustomizer,
                                      OpenApiCustomizer standardizedSuccessResponseCustomizer) {
    return moduleGroup(
      "users",
      "fit.iuh.kttkpm_nhom15_be.users.presentation.controllers",
      moduleTagCustomizer,
      standardizedErrorResponseCustomizer,
      standardizedSuccessResponseCustomizer
    );
  }

  private GroupedOpenApi moduleGroup(String groupName,
                                     String packageToScan,
                                     OpenApiCustomizer moduleTagCustomizer,
                                     OpenApiCustomizer standardizedErrorResponseCustomizer,
                                     OpenApiCustomizer standardizedSuccessResponseCustomizer) {
    return GroupedOpenApi.builder()
      .group(groupName)
      .packagesToScan(packageToScan)
      .pathsToMatch("/api/**")
      .addOpenApiCustomizer(moduleTagCustomizer)
      .addOpenApiCustomizer(standardizedErrorResponseCustomizer)
      .addOpenApiCustomizer(standardizedSuccessResponseCustomizer)
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
    if (path.startsWith("/api/v1/payments")) {
      return "Payments";
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

  private void addErrorResponse(ApiResponses responses, String statusCode, String description) {
    if (responses.containsKey(statusCode)) {
      return;
    }

    responses.addApiResponse(
      statusCode,
      new ApiResponse()
        .description(description)
        .content(new Content().addMediaType(
          org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
          new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiErrorResponse"))
        ))
    );
  }

  private void wrapSuccessResponse(String path,
                                   PathItem.HttpMethod httpMethod,
                                   String statusCode,
                                   ApiResponse apiResponse) {
    if (!isSuccessStatus(statusCode) || apiResponse.getContent() == null) {
      return;
    }

    Set<String> contentTypes = new HashSet<>(apiResponse.getContent().keySet());
    contentTypes.forEach(contentType -> {
      if (!org.springframework.http.MediaType.APPLICATION_JSON_VALUE.equals(contentType)) {
        return;
      }

      MediaType mediaType = apiResponse.getContent().get(contentType);
      if (mediaType == null || mediaType.getSchema() == null) {
        return;
      }

      mediaType.setSchema(buildSuccessEnvelopeSchema(path, httpMethod, statusCode, mediaType.getSchema()));
    });
  }

  private boolean isSuccessStatus(String statusCode) {
    return statusCode != null && statusCode.matches("2\\d\\d");
  }

  private Schema<?> buildSuccessEnvelopeSchema(String path,
                                               PathItem.HttpMethod httpMethod,
                                               String statusCode,
                                               Schema<?> dataSchema) {
    ObjectSchema schema = new ObjectSchema();
    schema.addRequiredItem("success");
    schema.addRequiredItem("timestamp");
    schema.addRequiredItem("status");
    schema.addRequiredItem("message");
    schema.addRequiredItem("path");

    schema.addProperty("success", new BooleanSchema().example(true));
    schema.addProperty("timestamp", new StringSchema().format("date-time").example("2026-05-10T10:20:30Z"));
    schema.addProperty("status", new IntegerSchema().example(Integer.parseInt(statusCode)));
    schema.addProperty("message", new StringSchema().example(resolveSuccessMessage(httpMethod, statusCode)));
    schema.addProperty("path", new StringSchema().example(path));

    if (!isMessageOnlySchema(dataSchema)) {
      schema.addProperty("data", dataSchema);
    }

    return schema;
  }

  private boolean isMessageOnlySchema(Schema<?> dataSchema) {
    return dataSchema.get$ref() != null && dataSchema.get$ref().endsWith("/MessageResponse");
  }

  private String resolveSuccessMessage(PathItem.HttpMethod httpMethod, String statusCode) {
    if ("201".equals(statusCode)) {
      return "Tao moi thanh cong";
    }

    if ("202".equals(statusCode)) {
      return "Yeu cau da duoc tiep nhan thanh cong";
    }

    if (httpMethod == null) {
      return "Yeu cau thanh cong";
    }

    return switch (httpMethod) {
      case GET, HEAD -> "Lay du lieu thanh cong";
      case POST -> "Xu ly thanh cong";
      case PUT, PATCH -> "Cap nhat thanh cong";
      case DELETE -> "Xoa thanh cong";
      default -> "Yeu cau thanh cong";
    };
  }
}
