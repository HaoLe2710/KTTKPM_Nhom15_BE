package fit.iuh.kttkpm_nhom15_be.search.application.dto;

public record SearchSuggestionDTO(
  String text,
  String source,
  int weight
) {}
