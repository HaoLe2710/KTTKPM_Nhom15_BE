package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.storage;

import fit.iuh.kttkpm_nhom15_be.shared.application.storage.FileStoragePort;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.StoredFile;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.UploadFileCommand;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.storage.s3", name = "enabled", havingValue = "false", matchIfMissing = true)
public class MockFileStorageAdapter implements FileStoragePort {

  @Override
  public StoredFile upload(UploadFileCommand command) {
    String filename = sanitizeFilename(command.originalFilename());
    String objectKey = "mock/" + command.scope() + "/" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + "-" + filename;
    String url = "https://mock-image-server.com/uploads/" + objectKey;
    log.warn("S3 is disabled. Falling back to mock storage for objectKey={}", objectKey);
    return new StoredFile(objectKey, url);
  }

  private String sanitizeFilename(String filename) {
    if (filename == null || filename.isBlank()) {
      return "file";
    }
    return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
  }
}
