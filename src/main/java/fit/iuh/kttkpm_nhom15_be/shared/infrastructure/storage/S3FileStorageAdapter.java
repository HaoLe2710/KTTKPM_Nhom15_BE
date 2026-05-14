package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.storage;

import fit.iuh.kttkpm_nhom15_be.shared.application.storage.FileStoragePort;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.StoredFile;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.UploadFileCommand;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.storage.s3", name = "enabled", havingValue = "true")
public class S3FileStorageAdapter implements FileStoragePort {

  private final S3Client s3Client;
  private final S3StorageProperties properties;

  @Override
  public StoredFile upload(UploadFileCommand command) {
    String objectKey = buildObjectKey(command.scope(), command.originalFilename());
    PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
      .bucket(properties.getBucket())
      .key(objectKey);

    if (command.contentType() != null && !command.contentType().isBlank()) {
      requestBuilder.contentType(command.contentType());
    }

    s3Client.putObject(requestBuilder.build(), software.amazon.awssdk.core.sync.RequestBody.fromBytes(command.bytes()));
    return new StoredFile(objectKey, buildPublicUrl(objectKey));
  }

  @Override
  public void delete(String objectKey) {
    if (objectKey == null || objectKey.isBlank()) {
      return;
    }

    try {
      s3Client.deleteObject(DeleteObjectRequest.builder()
        .bucket(properties.getBucket())
        .key(objectKey)
        .build());
    } catch (Exception ex) {
      log.warn("Cannot delete object '{}' from S3 bucket '{}': {}", objectKey, properties.getBucket(), ex.getMessage());
    }
  }

  private String buildObjectKey(String scope, String originalFilename) {
    String prefix = normalize(properties.getKeyPrefix());
    String scopePart = normalize(scope);
    String filename = sanitizeFilename(originalFilename);
    return prefix + "/" + scopePart + "/" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + "-" + filename;
  }

  private String buildPublicUrl(String objectKey) {
    if (properties.getCloudfrontUrl() != null && !properties.getCloudfrontUrl().isBlank()) {
      return normalizeBaseUrl(properties.getCloudfrontUrl()) + "/" + objectKey;
    }

    String region = Region.of(properties.getRegion()).id();
    return "https://" + properties.getBucket() + ".s3." + region + ".amazonaws.com/" + objectKey;
  }

  private String sanitizeFilename(String filename) {
    if (filename == null || filename.isBlank()) {
      return "file";
    }
    return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
  }

  private String normalize(String value) {
    if (value == null || value.isBlank()) {
      return "default";
    }
    return value
      .replace('\\', '/')
      .replaceAll("^/+", "")
      .replaceAll("/+$", "")
      .replaceAll("//+", "/");
  }

  private String normalizeBaseUrl(String value) {
    String normalized = value.trim();
    if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
      normalized = "https://" + normalized;
    }
    return normalized.replaceAll("/+$", "");
  }
}
