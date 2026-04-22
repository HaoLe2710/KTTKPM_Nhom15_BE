package fit.iuh.kttkpm_nhom15_be;

import fit.iuh.kttkpm_nhom15_be.shared.application.storage.FileStoragePort;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.StoredFile;
import fit.iuh.kttkpm_nhom15_be.shared.application.storage.UploadFileCommand;
import java.io.IOException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/auth/test-s3")
public class S3TestController {

    private final FileStoragePort fileStoragePort;

    public S3TestController(FileStoragePort fileStoragePort) {
        this.fileStoragePort = fileStoragePort;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadTestFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "File is empty"));
        }

        try {
            StoredFile storedFile = fileStoragePort.upload(new UploadFileCommand(
                "s3-test",
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes()
            ));

            return ResponseEntity.ok(Map.of(
                "message", "Upload success",
                "url", storedFile.url(),
                "objectKey", storedFile.objectKey(),
                "size", file.getSize()
            ));
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().body(Map.of(
                "message", "Cannot read uploaded file",
                "error", ex.getMessage()
            ));
        }
    }
}
