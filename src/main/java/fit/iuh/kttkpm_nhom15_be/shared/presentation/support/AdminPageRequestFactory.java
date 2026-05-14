package fit.iuh.kttkpm_nhom15_be.shared.presentation.support;

import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest.SortDirection;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AdminPageRequestFactory {

  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_SIZE = 20;
  private static final int MAX_SIZE = 100;

  public AdminPageRequest create(Integer page,
                                 Integer size,
                                 String sort,
                                 String defaultField,
                                 SortDirection defaultDirection,
                                 Set<String> allowedFields) {
    int resolvedPage = page == null ? DEFAULT_PAGE : page;
    int resolvedSize = size == null ? DEFAULT_SIZE : size;

    if (resolvedPage < 0) {
      throw new ApiValidationException("page must be greater than or equal to 0");
    }
    if (resolvedSize < 1 || resolvedSize > MAX_SIZE) {
      throw new ApiValidationException("size must be between 1 and 100");
    }

    String resolvedSort = sort == null || sort.isBlank()
      ? defaultField + "," + defaultDirection.name().toLowerCase()
      : sort.trim();
    String[] parts = resolvedSort.split(",");
    if (parts.length != 2) {
      throw new ApiValidationException("sort must use the format field,asc|desc");
    }

    String sortField = parts[0].trim();
    String sortDirectionRaw = parts[1].trim().toUpperCase();
    if (!allowedFields.contains(sortField)) {
      throw new ApiValidationException("Unsupported sort field: " + sortField);
    }
    if (!"ASC".equals(sortDirectionRaw) && !"DESC".equals(sortDirectionRaw)) {
      throw new ApiValidationException("sort direction must be asc or desc");
    }

    return new AdminPageRequest(resolvedPage, resolvedSize, sortField, SortDirection.valueOf(sortDirectionRaw));
  }
}
