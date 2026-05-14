package fit.iuh.kttkpm_nhom15_be.orders.application.usecases;

import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartDTO;
import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartItemDTO;
import fit.iuh.kttkpm_nhom15_be.carts.application.interfaces.CartFacade;
import fit.iuh.kttkpm_nhom15_be.catalog.application.interfaces.CatalogFacade;
import fit.iuh.kttkpm_nhom15_be.orders.application.commands.PlaceOrderCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.VariantSnapshot;
import fit.iuh.kttkpm_nhom15_be.orders.application.events.OrderPlacedEvent;
import fit.iuh.kttkpm_nhom15_be.orders.application.events.ProductSalesChangedEvent;
import fit.iuh.kttkpm_nhom15_be.orders.application.results.PlaceOrderResult;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderItem;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.AppliedPromotionDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartItemDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.interfaces.PromotionFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceOrderUseCase {

  private final OrderRepository orderRepository;
  private final CartFacade cartFacade;
  private final CatalogFacade catalogFacade;
  private final PromotionFacade promotionFacade;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public PlaceOrderResult execute(PlaceOrderCommand command) {
    CartDTO cart = cartFacade.getActiveCart(command.getUserId());

    List<CartItemDTO> cartItems = cart.getItems();
    List<VariantSnapshot> snapshots = catalogFacade.validateAndGetSnapshots(cartItems);

    Map<String, VariantSnapshot> snapshotMap = snapshots.stream()
      .collect(Collectors.toMap(VariantSnapshot::getVariantId, Function.identity()));

    List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
      VariantSnapshot snap = snapshotMap.get(cartItem.getVariantId());
      BigDecimal unitPrice = snap != null ? snap.getCurrentPrice() : cartItem.getPrice();
      BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

      return OrderItem.builder()
        .productId(snap != null ? snap.getProductId() : null)
        .variantId(cartItem.getVariantId())
        .sku(snap != null ? snap.getSku() : null)
        .name(snap != null ? snap.getProductName() : null)
        .imageUrl(snap != null ? snap.getImageUrl() : null)
        .optionsSnapshot(snap != null ? snap.getAttributes() : null)
        .quantity(cartItem.getQuantity())
        .unitPrice(unitPrice)
        .lineTotal(lineTotal)
        .build();
    }).toList();

    BigDecimal subtotal = orderItems.stream()
      .map(OrderItem::getLineTotal)
      .reduce(BigDecimal.ZERO, BigDecimal::add);

    AppliedPromotionDTO appliedPromotion = resolvePromotion(command, subtotal, orderItems);
    BigDecimal discount = appliedPromotion != null ? appliedPromotion.discountAmount() : BigDecimal.ZERO;
    BigDecimal shipping = command.getShippingFee();
    BigDecimal total = subtotal.subtract(discount).add(shipping);

    Order newOrder = Order.builder()
      .orderNo(generateOrderNo())
      .userId(command.getUserId())
      .subtotalAmount(subtotal)
      .discountAmount(discount)
      .shippingFee(shipping)
      .totalAmount(total)
      .promotionId(appliedPromotion != null ? appliedPromotion.promotionId() : null)
      .promotionCode(appliedPromotion != null ? appliedPromotion.promotionCode() : null)
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

    newOrder.initBehavior();

    Order savedOrder = orderRepository.save(newOrder);

    if (appliedPromotion != null) {
      promotionFacade.markPromotionUsed(appliedPromotion.promotionId());
    }

    cartFacade.clearCart(command.getUserId());

    eventPublisher.publishEvent(new OrderPlacedEvent(
      savedOrder.getId(),
      savedOrder.getOrderNo(),
      savedOrder.getUserId(),
      savedOrder.getTotalAmount()
    ));
    eventPublisher.publishEvent(new ProductSalesChangedEvent(
      orderItems.stream()
        .map(OrderItem::getProductId)
        .filter(productId -> productId != null && !productId.isBlank())
        .distinct()
        .toList(),
      "ORDER_PLACED",
      LocalDateTime.now()
    ));

    return PlaceOrderResult.builder()
      .orderId(savedOrder.getId())
      .orderNo(savedOrder.getOrderNo())
      .build();
  }

  private AppliedPromotionDTO resolvePromotion(PlaceOrderCommand command, BigDecimal subtotal, List<OrderItem> orderItems) {
    if (command.getPromotionCode() == null || command.getPromotionCode().isBlank()) {
      return null;
    }

    OrderCartDTO promotionCart = new OrderCartDTO(
      subtotal,
      orderItems.stream()
        .map(item -> new OrderCartItemDTO(item.getVariantId(), item.getQuantity(), item.getUnitPrice(), item.getLineTotal()))
        .toList()
    );
    return promotionFacade.validateAndCalculate(command.getPromotionCode(), promotionCart);
  }

  private String generateOrderNo() {
    return "ORD-" + System.currentTimeMillis();
  }
}
