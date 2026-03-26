package fit.iuh.kttkpm_nhom15_be.search.application.results;

import fit.iuh.kttkpm_nhom15_be.search.application.dto.SearchProductItemDTO;
import java.util.List;

public record SearchPageResult(List<SearchProductItemDTO> items, long total) {}
