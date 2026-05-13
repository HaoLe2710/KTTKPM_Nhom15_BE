package fit.iuh.kttkpm_nhom15_be.catalog.presentation.requests.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class BrandMultipartWriteRequest {

  @NotBlank
  private String code;

  @NotBlank
  private String name;

  private String slug;
  private String description;
  private Boolean isActive;

  // Optional:
  // - provide file to upload to S3 and auto-set logoUrl
  // - or provide logoUrl directly for backward compatibility
  private MultipartFile logoFile;
  private String logoUrl;
}

