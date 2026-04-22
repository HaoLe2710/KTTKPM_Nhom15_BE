package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
public class S3StorageConfig {

  @Bean
  @ConditionalOnProperty(prefix = "app.storage.s3", name = "enabled", havingValue = "true")
  S3Client s3Client(S3StorageProperties properties) {
    if (isBlank(properties.getBucket()) || isBlank(properties.getRegion())) {
      throw new IllegalStateException("S3 is enabled but bucket/region is missing. Configure app.storage.s3.bucket and app.storage.s3.region");
    }

    S3ClientBuilder builder = S3Client.builder()
      .region(Region.of(properties.getRegion()));

    if (!isBlank(properties.getAccessKey()) && !isBlank(properties.getSecretKey())) {
      builder.credentialsProvider(StaticCredentialsProvider.create(
        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
      ));
    } else {
      builder.credentialsProvider(DefaultCredentialsProvider.create());
    }

    return builder.build();
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
