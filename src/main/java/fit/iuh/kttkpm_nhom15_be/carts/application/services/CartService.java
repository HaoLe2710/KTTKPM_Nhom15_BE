package fit.iuh.kttkpm_nhom15_be.carts.application.services;

import fit.iuh.kttkpm_nhom15_be.carts.application.commands.AddToCartCommand;
import fit.iuh.kttkpm_nhom15_be.carts.application.dto.AddToCartResult;
import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartSummaryDTO;
import fit.iuh.kttkpm_nhom15_be.carts.application.usecases.AddToCartUseCase;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.ProductResponse;
import fit.iuh.kttkpm_nhom15_be.catalog.application.dto.ProductResponse.ProductVariantResponse;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

  private final AddToCartUseCase addToCartUseCase;
  private final UserRepository userRepository;

  public AddToCartResult addToCart(
    String userId,
    ProductResponse product,
    ProductVariantResponse variant,
    Integer quantity
  ) {
    String normalizedUserId = trimToNull(userId);
    if (normalizedUserId == null) {
      throw new ApiValidationException("Bạn cần đăng nhập trước khi thêm sản phẩm vào giỏ hàng.");
    }
    if (userRepository.findById(normalizedUserId).isEmpty()) {
      throw new ApiValidationException("Không tìm thấy tài khoản người dùng để thêm sản phẩm vào giỏ hàng.");
    }
    if (product == null || trimToNull(product.id()) == null) {
      throw new ApiValidationException("Không xác định được sản phẩm cần thêm vào giỏ hàng.");
    }
    if (variant == null || trimToNull(variant.id()) == null) {
      throw new ApiValidationException("Sản phẩm hiện chưa có biến thể còn hàng để thêm vào giỏ.");
    }

    int resolvedQuantity = quantity == null || quantity <= 0 ? 1 : quantity;
    if (!variant.active() || variant.stockQuantity() <= 0) {
      throw new ApiValidationException("Sản phẩm hiện đã hết hàng.");
    }
    if (variant.stockQuantity() < resolvedQuantity) {
      throw new ApiValidationException("Số lượng tồn kho không đủ cho yêu cầu của bạn.");
    }

    CartSummaryDTO summary = addToCartUseCase.execute(new AddToCartCommand(
      normalizedUserId,
      variant.id(),
      resolvedQuantity
    ));

    return new AddToCartResult(
      true,
      "Đã thêm sản phẩm vào giỏ hàng.",
      normalizedUserId,
      product.id(),
      product.name(),
      variant.id(),
      variant.sku(),
      resolvedQuantity,
      variant.price(),
      summary
    );
  }

  private String trimToNull(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
