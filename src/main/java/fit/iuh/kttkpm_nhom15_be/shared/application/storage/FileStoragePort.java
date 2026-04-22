package fit.iuh.kttkpm_nhom15_be.shared.application.storage;

public interface FileStoragePort {

  StoredFile upload(UploadFileCommand command);

  default void delete(String objectKey) {
    // Optional no-op for implementations that do not persist files externally.
  }
}
