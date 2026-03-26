package fit.iuh.kttkpm_nhom15_be.search.application.models;

import java.util.List;

public record SearchQueryContext(
  String locale,
  String normalizedQuery,
  String rawLowerQuery,
  String normalizedSku,
  List<String> expandedTerms
) {}
