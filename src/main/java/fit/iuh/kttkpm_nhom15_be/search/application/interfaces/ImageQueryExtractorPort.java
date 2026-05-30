package fit.iuh.kttkpm_nhom15_be.search.application.interfaces;

public interface ImageQueryExtractorPort {

  String extractQuery(byte[] imageBytes, String mimeType);
}

