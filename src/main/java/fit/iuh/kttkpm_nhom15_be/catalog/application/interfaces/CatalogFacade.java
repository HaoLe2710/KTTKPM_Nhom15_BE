package fit.iuh.kttkpm_nhom15_be.catalog.application.interfaces;

import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartItemDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.VariantInfoDTO;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.StockRestoreItem;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.VariantSnapshot;
import java.util.List;

public interface CatalogFacade {
  // Kiểm tra tồn kho và lấy snapshot dữ liệu (Tên, Giá, Cấu hình màu sắc/dung tích)
  // Nếu hết hàng, hàm này sẽ tự ném OutOfStockException
  List<VariantSnapshot> validateAndGetSnapshots(List<CartItemDTO> items);

  // Hoàn lại số lượng tồn kho khi đơn hàng bị hủy
  void restoreStock(List<StockRestoreItem> items);

  // Kiểm tra tồn kho và lấy giá cho AddToCartUseCase
  // Ném ProductUnavailableException nếu không đủ hàng
  VariantInfoDTO checkAvailabilityAndPrice(String variantId, int quantity);
}