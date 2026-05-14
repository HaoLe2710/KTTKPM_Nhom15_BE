package fit.iuh.kttkpm_nhom15_be.search.application.dto;

public record SearchFacetBucketDTO(
  String value,
  String label,
  long count
) {}
