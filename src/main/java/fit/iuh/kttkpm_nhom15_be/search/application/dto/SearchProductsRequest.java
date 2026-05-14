package fit.iuh.kttkpm_nhom15_be.search.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record SearchProductsRequest(
  String query,
  List<String> typeIds,
  BigDecimal minPrice,
  BigDecimal maxPrice,
  Boolean inStock,
  String sort,
  int page,
  int size
) {}
