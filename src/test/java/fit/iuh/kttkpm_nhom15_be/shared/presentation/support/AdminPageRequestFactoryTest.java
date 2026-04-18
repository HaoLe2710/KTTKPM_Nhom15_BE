package fit.iuh.kttkpm_nhom15_be.shared.presentation.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest.SortDirection;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AdminPageRequestFactoryTest {

  private final AdminPageRequestFactory factory = new AdminPageRequestFactory();

  @Test
  void createUsesLockedDefaultsWhenPaginationAndSortAreMissing() {
    var pageRequest = factory.create(null, null, null, "updatedAt", SortDirection.DESC, Set.of("updatedAt", "name"));

    assertEquals(0, pageRequest.page());
    assertEquals(20, pageRequest.size());
    assertEquals("updatedAt", pageRequest.sortField());
    assertEquals(SortDirection.DESC, pageRequest.sortDirection());
  }

  @Test
  void createRejectsOversizedPageSize() {
    assertThrows(ApiValidationException.class,
      () -> factory.create(0, 101, "updatedAt,desc", "updatedAt", SortDirection.DESC, Set.of("updatedAt")));
  }

  @Test
  void createRejectsUnsupportedSortField() {
    assertThrows(ApiValidationException.class,
      () -> factory.create(0, 20, "createdAt,desc", "updatedAt", SortDirection.DESC, Set.of("updatedAt")));
  }

  @Test
  void createRejectsInvalidSortFormat() {
    assertThrows(ApiValidationException.class,
      () -> factory.create(0, 20, "updatedAt", "updatedAt", SortDirection.DESC, Set.of("updatedAt")));
  }
}
