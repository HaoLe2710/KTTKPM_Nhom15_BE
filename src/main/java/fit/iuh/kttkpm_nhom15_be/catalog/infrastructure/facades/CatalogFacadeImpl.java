package fit.iuh.kttkpm_nhom15_be.catalog.infrastructure.facades;

import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartItemDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.VariantInfoDTO;
import fit.iuh.kttkpm_nhom15_be.catalog.application.interfaces.CatalogFacade;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.exceptions.ProductUnavailableException;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Media;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Product;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.Variant;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.models.VariantOption;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.OptionValueRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantRepository;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.StockRestoreItem;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.VariantSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of CatalogFacade interacting with the Catalog Domain via Repositories.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogFacadeImpl implements CatalogFacade {

  private final VariantRepository variantRepository;
  private final ProductRepository productRepository;
  private final OptionValueRepository optionValueRepository;
  private final OptionRepository optionRepository;

  @Override
  public List<VariantSnapshot> validateAndGetSnapshots(List<CartItemDTO> items) {
    return items.stream().map(item -> {
      Variant variant = variantRepository.findById(item.getVariantId())
        .orElseThrow(() -> new ProductUnavailableException(item.getVariantId()));

      String productName = "Unknown Product";
      if (variant.getProductId() != null) {
        productName = productRepository.findById(variant.getProductId())
          .map(Product::getName)
          .orElse("Unknown Product");
      }

      String imageUrl = null;
      if (variant.getMedia() != null) {
        imageUrl = variant.getMedia().stream()
          .filter(Media::isPrimary)
          .map(Media::getUrl)
          .findFirst()
          .orElse(null);
      }

      Map<String, Object> attributes = new HashMap<>();
      if (variant.getOptions() != null) {
        for (VariantOption vo : variant.getOptions()) {
          optionValueRepository.findById(vo.getOptionValueId()).ifPresent(ov -> {
            optionRepository.findById(ov.getOptionId()).ifPresent(opt -> {
              attributes.put(opt.getName(), ov.getValue());
            });
          });
        }
      }

      return VariantSnapshot.builder()
        .variantId(variant.getId())
        .sku(variant.getSku())
        .productName(productName)
        .imageUrl(imageUrl)
        .currentPrice(variant.getPrice())
        .attributes(attributes)
        .build();
    }).toList();
  }

  @Override
  public void restoreStock(List<StockRestoreItem> items) {
    // Stub: log và không làm gì — chờ xử lý transactional update
    items.forEach(item ->
      log.info("[STUB] Hoàn lại tồn kho: variantId={}, quantity={}", item.variantId(), item.quantity())
    );
  }

  @Override
  public VariantInfoDTO checkAvailabilityAndPrice(String variantId, int quantity) {
    log.info("Checking availability for variantId={}, requested quantity={}", variantId, quantity);
    
    // 1. Phân giải Variant Domain Model
    Variant variant = variantRepository.findById(variantId)
      .orElseThrow(() -> new ProductUnavailableException(variantId));

    // 2. Kiểm tra tồn kho từ Domain Model
    if (variant.getStockQuantity() < quantity) {
      log.warn("Not enough stock for variantId={}. Available: {}, Requested: {}", variantId, variant.getStockQuantity(), quantity);
      throw new ProductUnavailableException(variantId);
    }

    // 3. Phân giải Product Domain Model để lấy tên gốc
    String productName = "Unknown Product";
    if (variant.getProductId() != null) {
      productName = productRepository.findById(variant.getProductId())
        .map(Product::getName)
        .orElse("Unknown Product");
    }

    // 4. Trả về thông tin
    return VariantInfoDTO.builder()
      .name(productName + (variant.getSku() != null ? " (" + variant.getSku() + ")" : ""))
      .price(variant.getPrice())
      .build();
  }

  @Override
  public boolean checkVariantsExist(List<String> variantIds) {
    if (variantIds == null || variantIds.isEmpty()) {
      return false;
    }

    List<String> normalizedIds = variantIds.stream()
      .filter(Objects::nonNull)
      .map(String::trim)
      .filter(id -> !id.isEmpty())
      .distinct()
      .toList();

    if (normalizedIds.isEmpty()) {
      return false;
    }

    return variantRepository.countExistingByIds(normalizedIds) == normalizedIds.size();
  }
}
