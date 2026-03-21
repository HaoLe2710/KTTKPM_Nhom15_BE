package fit.iuh.kttkpm_nhom15_be.catalog.application.dto;

import lombok.Builder;
import java.math.BigDecimal;

@Builder
public record VariantInfoDTO(
  BigDecimal price,
  String name
) {}
