package fit.iuh.kttkpm_nhom15_be.catalog.application.interfaces;

import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartItemDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.VariantInfoDTO;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.StockRestoreItem;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.VariantSnapshot;

import java.util.List;

public interface CatalogFacade {
  List<VariantSnapshot> validateAndGetSnapshots(List<CartItemDTO> items);

  void deductStock(List<CartItemDTO> items);

  void restoreStock(List<StockRestoreItem> items);

  VariantInfoDTO checkAvailabilityAndPrice(String variantId, int quantity);

  boolean checkVariantsExist(List<String> variantIds);
}
