package fit.iuh.kttkpm_nhom15_be.shared.application.cache;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

public record CachedPage<T>(
  List<T> content,
  int page,
  int size,
  long totalElements
) {

  public static <T> CachedPage<T> from(Page<T> page) {
    return new CachedPage<>(new ArrayList<>(page.getContent()), page.getNumber(), page.getSize(), page.getTotalElements());
  }

  public static <T> CachedPage<T> empty(int page, int size) {
    return new CachedPage<>(new ArrayList<>(), page, size, 0);
  }

  public Page<T> toPage() {
    return new PageImpl<>(content == null ? new ArrayList<>() : content, PageRequest.of(page, size), totalElements);
  }
}
