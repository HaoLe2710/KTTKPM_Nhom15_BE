package fit.iuh.kttkpm_nhom15_be.search.application.dto;

import java.util.List;

public record SearchResponseDTO(
  List<SearchProductItemDTO> items,
  List<SearchFacetDTO> facets,
  int page,
  int size,
  long total,
  String query,
  String normalizedQuery,
  SearchRedirectDTO redirect
) {}
