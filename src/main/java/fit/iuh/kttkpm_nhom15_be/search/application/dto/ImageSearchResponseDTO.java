package fit.iuh.kttkpm_nhom15_be.search.application.dto;

public record ImageSearchResponseDTO(
  String extractedQuery,
  SearchResponseDTO result
) {}

