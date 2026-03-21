package fit.iuh.kttkpm_nhom15_be.catalog.domain.exceptions;

public class ProductUnavailableException extends RuntimeException {
  public ProductUnavailableException(String variantId) {
    super("Sản phẩm với Variant ID '" + variantId + "' không đủ số lượng tồn kho hoặc đã ngừng bán.");
  }
}
