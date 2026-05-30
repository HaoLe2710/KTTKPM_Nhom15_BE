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
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.MediaRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.ProductRepository;
import fit.iuh.kttkpm_nhom15_be.catalog.domain.repositories.VariantRepository;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.StockRestoreItem;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.VariantSnapshot;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogFacadeImpl implements CatalogFacade {

  private final VariantRepository variantRepository;
  private final ProductRepository productRepository;
  private final MediaRepository mediaRepository;
  private final OptionValueRepository optionValueRepository;
  private final OptionRepository optionRepository;

  @Override
  public List<VariantSnapshot> validateAndGetSnapshots(List<CartItemDTO> items) {
    return items.stream().map(item -> {
      Variant variant = variantRepository.findById(item.getVariantId())
        .orElseThrow(() -> new ProductUnavailableException(item.getVariantId()));

      if (item.getQuantity() <= 0 || variant.getStockQuantity() < item.getQuantity()) {
        throw new ProductUnavailableException(item.getVariantId());
      }

      String productName = "Unknown Product";
      if (variant.getProductId() != null) {
        productName = productRepository.findById(variant.getProductId())
          .map(Product::getName)
          .orElse("Unknown Product");
      }

      String imageUrl = resolveSnapshotImageUrl(variant);

      Map<String, Object> attributes = new HashMap<>();
      if (variant.getOptions() != null) {
        for (VariantOption variantOption : variant.getOptions()) {
          optionValueRepository.findById(variantOption.getOptionValueId()).ifPresent(optionValue ->
            optionRepository.findById(optionValue.getOptionId()).ifPresent(option ->
              attributes.put(option.getName(), optionValue.getValue())
            )
          );
        }
      }

      return VariantSnapshot.builder()
        .variantId(variant.getId())
        .productId(variant.getProductId())
        .sku(variant.getSku())
        .productName(productName)
        .imageUrl(imageUrl)
        .currentPrice(variant.getPrice())
        .attributes(attributes)
        .build();
    }).toList();
  }

  private String resolveSnapshotImageUrl(Variant variant) {
    String variantMediaUrl = pickPrimaryImageUrl(variant.getMedia());
    if (variantMediaUrl != null) {
      return variantMediaUrl;
    }

    if (variant.getProductId() == null || variant.getProductId().isBlank()) {
      return null;
    }

    return pickPrimaryImageUrl(mediaRepository.findProductMedia(variant.getProductId()));
  }

  private String pickPrimaryImageUrl(List<Media> media) {
    if (media == null || media.isEmpty()) {
      return null;
    }

    List<Media> imageMedia = media.stream()
      .filter(this::isImageMedia)
      .toList();

    Media selectedMedia = imageMedia.stream()
      .filter(Media::isPrimary)
      .findFirst()
      .orElse(imageMedia.isEmpty() ? null : imageMedia.getFirst());

    return selectedMedia != null ? selectedMedia.getUrl() : null;
  }

  private boolean isImageMedia(Media media) {
    return media != null
      && media.getUrl() != null
      && !media.getUrl().isBlank()
      && (media.getType() == null || "IMAGE".equalsIgnoreCase(media.getType().name()));
  }

  @Override
  public void deductStock(List<CartItemDTO> items) {
    items.forEach(item -> {
      if (item.getQuantity() <= 0) {
        throw new ApiValidationException("Cart item quantity must be greater than 0.");
      }

      boolean deducted = variantRepository.deductStock(item.getVariantId(), item.getQuantity());
      if (!deducted) {
        throw new ProductUnavailableException(item.getVariantId());
      }
    });
  }

  @Override
  public void restoreStock(List<StockRestoreItem> items) {
    items.forEach(item -> {
      if (item.quantity() <= 0) {
        throw new ApiValidationException("Restore quantity must be greater than 0.");
      }

      boolean restored = variantRepository.restoreStock(item.variantId(), item.quantity());
      if (!restored) {
        throw new ApiValidationException("Unable to restore stock for variantId=" + item.variantId());
      }

      log.info("Restored stock for variantId={}, quantity={}", item.variantId(), item.quantity());
    });
  }

  @Override
  public VariantInfoDTO checkAvailabilityAndPrice(String variantId, int quantity) {
    log.info("Checking availability for variantId={}, requested quantity={}", variantId, quantity);

    Variant variant = variantRepository.findById(variantId)
      .orElseThrow(() -> new ProductUnavailableException(variantId));

    if (variant.getStockQuantity() < quantity) {
      log.warn("Not enough stock for variantId={}. Available: {}, Requested: {}", variantId, variant.getStockQuantity(), quantity);
      throw new ProductUnavailableException(variantId);
    }

    String productName = "Unknown Product";
    if (variant.getProductId() != null) {
      productName = productRepository.findById(variant.getProductId())
        .map(Product::getName)
        .orElse("Unknown Product");
    }

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
