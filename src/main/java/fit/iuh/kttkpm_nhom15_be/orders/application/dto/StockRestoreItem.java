package fit.iuh.kttkpm_nhom15_be.orders.application.dto;

/**
 * DTO đại diện cho một mục cần hoàn lại tồn kho khi hủy đơn hàng.
 * Được truyền sang CatalogFacade.restoreStock().
 */
public record StockRestoreItem(
  String variantId,
  int quantity
) {}
