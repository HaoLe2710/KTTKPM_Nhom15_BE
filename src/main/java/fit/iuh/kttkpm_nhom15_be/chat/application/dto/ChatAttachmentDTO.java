package fit.iuh.kttkpm_nhom15_be.chat.application.dto;

public record ChatAttachmentDTO(
        String url,
        String objectKey,
        String contentType,
        String originalFilename,
        long size
) {
}
