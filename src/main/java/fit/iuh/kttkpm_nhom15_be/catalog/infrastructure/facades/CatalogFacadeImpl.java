package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.facades;

import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartItemDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.interfaces.CatalogFacade;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.VariantSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Stub implementation of CatalogFacade.
 * TODO: Replace with real JPA-backed implementation querying variants, options, and media tables.
 */
@Component
public class CatalogFacadeImpl implements CatalogFacade {

  @Override
  public List<VariantSnapshot> validateAndGetSnapshots(List<CartItemDTO> items) {
    // Stub: trả về snapshot mẫu cho mỗi item trong giỏ
    // TODO: truy vấn bảng variants + variant_options + media theo variantId
    //       ném OutOfStockException nếu stock_quantity < requested quantity
    return items.stream().map(item -> VariantSnapshot.builder()
      .variantId(item.getVariantId())
      .sku("SKU-" + item.getVariantId())
      .productName("Sản phẩm Demo")
      .imageUrl("https://example.com/images/" + item.getVariantId() + ".jpg")
      .currentPrice(item.getPrice())
      .attributes(Map.of("Màu sắc", "Đỏ", "Dung tích", "50ml"))
      .build()
    ).toList();
  }
}
