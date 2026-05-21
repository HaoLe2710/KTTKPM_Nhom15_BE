package fit.iuh.kttkpm_nhom15_be.shared.presentation.controllers;

import fit.iuh.kttkpm_nhom15_be.shared.infrastructure.storage.MockFileStorageAdapter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/files")
public class StoredFileController {

  @GetMapping("/**")
  public ResponseEntity<Resource> getFile(HttpServletRequest request) throws IOException {
    String prefix = "/api/v1/files/";
    String requestUri = request.getRequestURI();
    String objectKey = requestUri.substring(requestUri.indexOf(prefix) + prefix.length());
    Path target = MockFileStorageAdapter.LOCAL_STORAGE_ROOT.resolve(objectKey).normalize();
    if (!target.startsWith(MockFileStorageAdapter.LOCAL_STORAGE_ROOT) || !Files.exists(target)) {
      return ResponseEntity.notFound().build();
    }

    String contentType = Files.probeContentType(target);
    Resource resource = new UrlResource(target.toUri());
    return ResponseEntity.ok()
            .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
  }
}
