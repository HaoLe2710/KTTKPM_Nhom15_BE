package fit.iuh.kttkpm_nhom15_be.shared.infrastructure.storage;

import fit.iuh.kttkpm_nhom15_be.shared.application.storage.FileStoragePort;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.StoredFile;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.UploadFileCommand;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "app.storage.s3", name = "enabled", havingValue = "false", matchIfMissing = true)
public class MockFileStorageAdapter implements FileStoragePort {

  public static final Path LOCAL_STORAGE_ROOT = Path.of(System.getProperty("java.io.tmpdir"), "kttkpm-nhom15-uploads");

  @Override
  public StoredFile upload(UploadFileCommand command) {
    String filename = sanitizeFilename(command.originalFilename());
    String scope = sanitizePathPart(command.scope());
    String objectKey = "local/" + scope + "/" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID() + "-" + filename;
    Path target = LOCAL_STORAGE_ROOT.resolve(objectKey).normalize();
    try {
      Files.createDirectories(target.getParent());
      Files.write(target, command.bytes());
    } catch (IOException ex) {
      throw new IllegalStateException("Cannot store uploaded file locally", ex);
    }
    String url = "/api/v1/files/" + objectKey;
    log.warn("S3 is disabled. Falling back to local storage for objectKey={}", objectKey);
    return new StoredFile(objectKey, url);
  }

  private String sanitizeFilename(String filename) {
    if (filename == null || filename.isBlank()) {
      return "file";
    }
    return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
  }

  private String sanitizePathPart(String value) {
    if (value == null || value.isBlank()) {
      return "general";
    }
    return value.replaceAll("[^a-zA-Z0-9._-]", "_");
  }
}
