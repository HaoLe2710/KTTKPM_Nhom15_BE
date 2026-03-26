package fit.iuh.kttkpm_nhom15_be.search.application.dto;

import java.util.List;

public record SearchFacetDTO(
  String key,
  List<SearchFacetBucketDTO> buckets
) {}
