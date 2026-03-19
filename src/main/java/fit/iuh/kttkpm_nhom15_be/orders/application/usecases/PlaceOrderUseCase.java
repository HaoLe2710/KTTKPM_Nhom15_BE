package fit.iuh.kttkpm_nhom15_be.orders.application.usecases;

import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartDTO;
import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartItemDTO;
import fit.iuh.kttkpm_nhom15_be.carts.application.interfaces.CartFacade;
import fit.iuh.kttkpm_nhom15_be.catalog.application.interfaces.CatalogFacade;
import fit.iuh.kttkpm_nhom15_be.orders.application.commands.PlaceOrderCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.VariantSnapshot;
import fit.iuh.kttkpm_nhom15_be.orders.application.events.OrderPlacedEvent;
import fit.iuh.kttkpm_nhom15_be.orders.application.results.PlaceOrderResult;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.*;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceOrderUseCase {

  private final OrderRepository orderRepository;
  private final CartFacade cartFacade;
  private final CatalogFacade catalogFacade;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public PlaceOrderResult execute(PlaceOrderCommand command) {
    // 1. Kiểm tra giỏ hàng (CartFacade ném EmptyCartException nếu rỗng)
    CartDTO cart = cartFacade.getActiveCart(command.getUserId());

    // 2. Kiểm tra tồn kho & lấy snapshot từ Catalog module
    List<CartItemDTO> cartItems = cart.getItems();
    List<VariantSnapshot> snapshots = catalogFacade.validateAndGetSnapshots(cartItems);

    // Tạo lookup map: variantId → VariantSnapshot (để map nhanh O(n))
    Map<String, VariantSnapshot> snapshotMap = snapshots.stream()
      .collect(Collectors.toMap(VariantSnapshot::getVariantId, Function.identity()));

    // 3. Xây dựng OrderItem — ghép CartItem + VariantSnapshot
    List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
      VariantSnapshot snap = snapshotMap.get(cartItem.getVariantId());
      BigDecimal lineTotal = cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
      return OrderItem.builder()
        .variantId(cartItem.getVariantId())
        .sku(snap != null ? snap.getSku() : null)
        .name(snap != null ? snap.getProductName() : null)
        .imageUrl(snap != null ? snap.getImageUrl() : null)
        .optionsSnapshot(snap != null ? snap.getAttributes() : null)
        .quantity(cartItem.getQuantity())
        .unitPrice(cartItem.getPrice())
        .lineTotal(lineTotal)
        .build();
    }).toList();

    // 4. Tính toán tiền
    BigDecimal subtotal = orderItems.stream()
      .map(OrderItem::getLineTotal)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal discount = BigDecimal.ZERO; // TODO: coupon/promotion logic
    BigDecimal shipping = command.getShippingFee();
    BigDecimal total = subtotal.subtract(discount).add(shipping);

    // 5. Khởi tạo Order domain model
    Order newOrder = Order.builder()
      .orderNo(generateOrderNo())
      .userId(command.getUserId())
      .subtotalAmount(subtotal)
      .discountAmount(discount)
      .shippingFee(shipping)
      .totalAmount(total)
      .status(OrderStatus.CREATED)
      .paymentMethod(command.getPaymentMethod())
      .paymentStatus(PaymentStatus.UNPAID)
      .shipFullName(command.getShipFullName())
      .shipPhone(command.getShipPhone())
      .shipAddress(command.getShipAddress())
      .shipCity(command.getShipCity())
      .shipDistrict(command.getShipDistrict())
      .shipWard(command.getShipWard())
      .shippingMode(command.getShippingMode())
      .shippingProvider(command.getShippingProvider())
      .items(orderItems)
      .build();

    // 6. Kích hoạt State Pattern (gán CreatedState)
    newOrder.initBehavior();

    // 7. Lưu vào DB
    Order savedOrder = orderRepository.save(newOrder);

    // 8. Dọn giỏ hàng
    cartFacade.clearCart(command.getUserId());

    // 9. Publish domain event — các module khác (payments, notifications) subscribe tại đây
    eventPublisher.publishEvent(new OrderPlacedEvent(
      savedOrder.getId(),
      savedOrder.getOrderNo(),
      savedOrder.getUserId(),
      savedOrder.getTotalAmount()
    ));

    return PlaceOrderResult.builder()
      .orderId(savedOrder.getId())
      .orderNo(savedOrder.getOrderNo())
      .build();
  }

  private String generateOrderNo() {
    return "ORD-" + System.currentTimeMillis();
  }
}