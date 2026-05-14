package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.storage.s3")
public class S3StorageProperties {

  private boolean enabled = false;
  private String bucket;
  private String region;
  private String accessKey;
  private String secretKey;
  private String cloudfrontUrl;
  private String keyPrefix = "uploads";
}
