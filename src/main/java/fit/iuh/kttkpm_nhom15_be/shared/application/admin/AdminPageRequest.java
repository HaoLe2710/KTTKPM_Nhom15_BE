package fit.iuh.kttkpm_nhom15_be.shared.application.admin;

public record AdminPageRequest(
  int page,
  int size,
  String sortField,
  SortDirection sortDirection
) {

  public enum SortDirection {
    ASC,
    DESC
  }
}
