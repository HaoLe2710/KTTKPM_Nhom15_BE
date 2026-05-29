package fit.iuh.kttkpm_nhom15_be.shared.application.storage;

public record UploadFileCommand(
  String scope,
  String originalFilename,
  String contentType,
  byte[] bytes
) {
}
