package fit.iuh.kttkpm_nhom15_be.orders.application.usecases;

import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartDTO;
import fit.iuh.kttkpm_nhom15_be.carts.application.dto.CartItemDTO;
import fit.iuh.kttkpm_nhom15_be.carts.application.interfaces.CartFacade;
import fit.iuh.kttkpm_nhom15_be.catalog.application.interfaces.CatalogFacade;
import fit.iuh.kttkpm_nhom15_be.orders.application.commands.PlaceOrderCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.commands.QuoteShippingFeeCommand;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.ShippingFeeQuoteDTO;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.VariantSnapshot;
import fit.iuh.kttkpm_nhom15_be.orders.application.events.OrderPlacedEvent;
import fit.iuh.kttkpm_nhom15_be.orders.application.events.ProductSalesChangedEvent;
import fit.iuh.kttkpm_nhom15_be.orders.application.results.PlaceOrderResult;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.Order;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderItem;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderRepository;
import fit.iuh.kttkpm_nhom15_be.payments.application.dto.PaymentTransactionResponse;
import fit.iuh.kttkpm_nhom15_be.payments.application.usecases.CreatePaymentUseCase;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.AppliedPromotionDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.dto.OrderCartItemDTO;
import fit.iuh.kttkpm_nhom15_be.promotions.application.interfaces.PromotionFacade;
import fit.iuh.kttkpm_nhom15_be.shared.application.exceptions.ApiValidationException;
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
  private final CreatePaymentUseCase createPaymentUseCase;
  private final QuoteShippingFeeUseCase quoteShippingFeeUseCase;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public PlaceOrderResult execute(PlaceOrderCommand command) {
    rejectGuestVoucher(command);

    CartDTO cart = cartFacade.getActiveCart(command.getUserId());

    List<CartItemDTO> cartItems = cart.getItems();
    List<VariantSnapshot> snapshots = catalogFacade.validateAndGetSnapshots(cartItems);
    catalogFacade.deductStock(cartItems);

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

    OrderCartDTO promotionCart = buildPromotionCart(subtotal, orderItems);
    List<AppliedPromotionDTO> productPromotions = promotionFacade.findAutomaticProductDiscounts(promotionCart);
    if (productPromotions == null) {
      productPromotions = List.of();
    }
    BigDecimal productDiscount = productPromotions.stream()
      .map(AppliedPromotionDTO::discountAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal subtotalAfterProductDiscount = subtotal.subtract(productDiscount).max(BigDecimal.ZERO);
    AppliedPromotionDTO voucherPromotion = resolveVoucherPromotion(command, subtotalAfterProductDiscount, orderItems);
    BigDecimal voucherDiscount = voucherPromotion != null ? voucherPromotion.discountAmount() : BigDecimal.ZERO;
    BigDecimal discount = productDiscount.add(voucherDiscount).min(subtotal);
    BigDecimal shipping = resolveShippingFee(command, subtotalAfterProductDiscount, orderItems);
    BigDecimal total = subtotal.subtract(discount).add(shipping).max(BigDecimal.ZERO);
    AppliedPromotionDTO primaryPromotion = voucherPromotion != null
      ? voucherPromotion
      : productPromotions.stream().findFirst().orElse(null);

    Order newOrder = Order.builder()
      .orderNo(generateOrderNo())
      .userId(command.getUserId())
      .subtotalAmount(subtotal)
      .discountAmount(discount)
      .shippingFee(shipping)
      .totalAmount(total)
      .promotionId(primaryPromotion != null ? primaryPromotion.promotionId() : null)
      .promotionCode(primaryPromotion != null ? primaryPromotion.promotionCode() : null)
      .status(OrderStatus.CREATED)
      .paymentMethod(command.getPaymentMethod())
      .paymentStatus(PaymentStatus.UNPAID)
      .stockDeducted(true)
      .shipFullName(command.getShipFullName())
      .shipPhone(command.getShipPhone())
      .shipEmail(command.getShipEmail())
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
    PaymentTransactionResponse paymentTransactionResponse = createPaymentUseCase.execute(savedOrder, command.getClientIp());

    for (AppliedPromotionDTO productPromotion : productPromotions) {
      promotionFacade.markPromotionUsed(productPromotion.promotionId());
    }
    if (voucherPromotion != null && productPromotions.stream().noneMatch(productPromotion -> voucherPromotion.promotionId().equals(productPromotion.promotionId()))) {
      promotionFacade.markPromotionUsed(voucherPromotion.promotionId());
    }

    cartFacade.clearCart(command.getUserId());

    eventPublisher.publishEvent(new OrderPlacedEvent(
      savedOrder.getId(),
      savedOrder.getOrderNo(),
      savedOrder.getUserId(),
      command.getShipEmail(),
      command.getShipFullName(),
      command.getShipPhone(),
      formatShippingAddress(command),
      command.getPaymentMethod() != null ? command.getPaymentMethod().name() : "",
      savedOrder.getSubtotalAmount(),
      savedOrder.getDiscountAmount(),
      savedOrder.getShippingFee(),
      savedOrder.getTotalAmount(),
      orderItems.stream()
        .map(item -> new OrderPlacedEvent.Item(
          item.getName(),
          item.getSku(),
          item.getQuantity(),
          item.getUnitPrice(),
          item.getLineTotal()
        ))
        .toList()
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
      .paymentRedirectUrl(paymentTransactionResponse.getPaymentRedirectUrl())
      .paymentInfo(paymentTransactionResponse.getPaymentInfo())
      .build();
  }

  private void rejectGuestVoucher(PlaceOrderCommand command) {
    if (command.getPromotionCode() == null || command.getPromotionCode().isBlank()) {
      return;
    }
    if (command.getUserId() == null || command.getUserId().isBlank() || command.getUserId().startsWith("guest-")) {
      throw new ApiValidationException("Voucher chỉ áp dụng cho khách hàng đã đăng nhập.");
    }
  }

  private AppliedPromotionDTO resolveVoucherPromotion(PlaceOrderCommand command, BigDecimal subtotal, List<OrderItem> orderItems) {
    if (command.getPromotionCode() == null || command.getPromotionCode().isBlank()) {
      return null;
    }

    return promotionFacade.validateOrderDiscountAndCalculate(command.getPromotionCode(), buildPromotionCart(subtotal, orderItems), command.getUserId());
  }

  private OrderCartDTO buildPromotionCart(BigDecimal subtotal, List<OrderItem> orderItems) {
    return new OrderCartDTO(
      subtotal,
      orderItems.stream()
        .map(item -> new OrderCartItemDTO(item.getVariantId(), item.getQuantity(), item.getUnitPrice(), item.getLineTotal()))
        .toList()
    );
  }

  private BigDecimal resolveShippingFee(PlaceOrderCommand command, BigDecimal orderValue, List<OrderItem> orderItems) {
    if (command.getShippingMode() != fit.iuh.kttkpm_nhom15_be.orders.domain.models.ShippingMode.PROVIDER_API) {
      return command.getShippingFee();
    }

    if (command.getShippingProvider() == null) {
      throw new ApiValidationException("shippingProvider không được để trống khi tính phí qua đơn vị vận chuyển.");
    }

    int itemQuantity = orderItems.stream()
      .map(OrderItem::getQuantity)
      .reduce(0, Integer::sum);

    ShippingFeeQuoteDTO quote = quoteShippingFeeUseCase.execute(new QuoteShippingFeeCommand(
      command.getShippingProvider(),
      command.getShipAddress(),
      command.getShipCity(),
      command.getShipDistrict(),
      command.getShipWard(),
      orderValue,
      itemQuantity
    ));

    if (!quote.deliverySupported()) {
      throw new ApiValidationException(quote.message());
    }

    return quote.fee();
  }

  private String generateOrderNo() {
    return "ORD-" + System.currentTimeMillis();
  }

  private String formatShippingAddress(PlaceOrderCommand command) {
    return List.of(
        command.getShipAddress(),
        command.getShipWard(),
        command.getShipDistrict(),
        command.getShipCity()
      ).stream()
      .filter(value -> value != null && !value.isBlank())
      .collect(Collectors.joining(", "));
  }
}
