package fit.iuh.kttkpm_nhom15_be.orders.infrastructure.persistence.repositories;

import fit.iuh.kttkpm_nhom15_be.orders.application.dto.admin.OrderAdminDtos.OrderAdminDetailResponse;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.admin.OrderAdminDtos.OrderAdminItemRow;
import fit.iuh.kttkpm_nhom15_be.orders.application.dto.admin.OrderAdminDtos.OrderAdminSummaryRow;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.OrderStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.models.PaymentStatus;
import fit.iuh.kttkpm_nhom15_be.orders.domain.repositories.OrderAdminRepository;
import fit.iuh.kttkpm_nhom15_be.shared.application.admin.AdminPageRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderAdminRepositoryImpl implements OrderAdminRepository {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public Page<OrderAdminSummaryRow> findOrders(String query,
                                               OrderStatus status,
                                               PaymentStatus paymentStatus,
                                               AdminPageRequest pageRequest) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    String where = buildWhere(query, status, paymentStatus, params);

    String sql = """
      SELECT o.id,
             o.order_no,
             o.user_id,
             u.full_name AS customer_name,
             u.email AS customer_email,
             o.status,
             o.payment_method,
             o.payment_status,
             o.total_amount,
             (
               SELECT COUNT(*)
               FROM order_items oi
               WHERE oi.order_id = o.id
             ) AS item_count,
             o.created_at
      FROM orders o
      LEFT JOIN users u ON u.id = o.user_id
      """ + where
      + " ORDER BY " + toOrderBy(pageRequest)
      + " LIMIT :limit OFFSET :offset ";

    params.addValue("limit", pageRequest.size());
    params.addValue("offset", pageRequest.page() * pageRequest.size());

    List<OrderAdminSummaryRow> content = jdbcTemplate.query(sql, params, (rs, rowNum) ->
      new OrderAdminSummaryRow(
        rs.getString("id"),
        rs.getString("order_no"),
        rs.getString("user_id"),
        rs.getString("customer_name"),
        rs.getString("customer_email"),
        toOrderStatus(rs.getString("status")),
        toPaymentMethod(rs.getString("payment_method")),
        toPaymentStatus(rs.getString("payment_status")),
        rs.getBigDecimal("total_amount"),
        rs.getInt("item_count"),
        rs.getObject("created_at", LocalDateTime.class)
      )
    );

    Long total = jdbcTemplate.queryForObject(
      "SELECT COUNT(*) FROM orders o LEFT JOIN users u ON u.id = o.user_id " + where,
      params,
      Long.class
    );

    return new PageImpl<>(
      content,
      PageRequest.of(pageRequest.page(), pageRequest.size()),
      total == null ? 0 : total
    );
  }

  @Override
  public Optional<OrderAdminDetailResponse> findOrderDetail(String orderId) {
    String detailSql = """
      SELECT o.id,
             o.order_no,
             o.user_id,
             u.full_name AS customer_name,
             u.email AS customer_email,
             o.ship_full_name,
             o.ship_phone,
             o.ship_address,
             o.ship_city,
             o.ship_district,
             o.ship_ward,
             o.status,
             o.payment_method,
             o.payment_status,
             o.subtotal_amount,
             o.discount_amount,
             o.shipping_fee,
             o.total_amount,
             o.created_at,
             o.updated_at
      FROM orders o
      LEFT JOIN users u ON u.id = o.user_id
      WHERE o.id = CAST(:orderId AS UUID)
      """;

    List<OrderAdminDetailResponse> rows = jdbcTemplate.query(
      detailSql,
      new MapSqlParameterSource("orderId", orderId),
      (rs, rowNum) -> new OrderAdminDetailResponse(
        rs.getString("id"),
        rs.getString("order_no"),
        rs.getString("user_id"),
        rs.getString("customer_name"),
        rs.getString("customer_email"),
        rs.getString("ship_full_name"),
        rs.getString("ship_phone"),
        rs.getString("ship_address"),
        rs.getString("ship_city"),
        rs.getString("ship_district"),
        rs.getString("ship_ward"),
        toOrderStatus(rs.getString("status")),
        toPaymentMethod(rs.getString("payment_method")),
        toPaymentStatus(rs.getString("payment_status")),
        rs.getBigDecimal("subtotal_amount"),
        rs.getBigDecimal("discount_amount"),
        rs.getBigDecimal("shipping_fee"),
        rs.getBigDecimal("total_amount"),
        rs.getObject("created_at", LocalDateTime.class),
        rs.getObject("updated_at", LocalDateTime.class),
        List.of()
      )
    );

    if (rows.isEmpty()) {
      return Optional.empty();
    }

    OrderAdminDetailResponse base = rows.get(0);
    List<OrderAdminItemRow> items = findItems(orderId);
    return Optional.of(new OrderAdminDetailResponse(
      base.id(),
      base.orderNo(),
      base.userId(),
      base.customerName(),
      base.customerEmail(),
      base.shipFullName(),
      base.shipPhone(),
      base.shipAddress(),
      base.shipCity(),
      base.shipDistrict(),
      base.shipWard(),
      base.status(),
      base.paymentMethod(),
      base.paymentStatus(),
      base.subtotalAmount(),
      base.discountAmount(),
      base.shippingFee(),
      base.totalAmount(),
      base.createdAt(),
      base.updatedAt(),
      items
    ));
  }

  private List<OrderAdminItemRow> findItems(String orderId) {
    String sql = """
      SELECT oi.id,
             oi.product_id,
             oi.variant_id,
             oi.sku,
             oi.name,
             oi.image_url,
             oi.quantity,
             oi.unit_price,
             oi.line_total
      FROM order_items oi
      WHERE oi.order_id = CAST(:orderId AS UUID)
      ORDER BY oi.created_at
      """;
    return jdbcTemplate.query(sql, new MapSqlParameterSource("orderId", orderId), (rs, rowNum) ->
      new OrderAdminItemRow(
        rs.getString("id"),
        rs.getString("product_id"),
        rs.getString("variant_id"),
        rs.getString("sku"),
        rs.getString("name"),
        rs.getString("image_url"),
        rs.getInt("quantity"),
        rs.getBigDecimal("unit_price"),
        rs.getBigDecimal("line_total")
      )
    );
  }

  private String buildWhere(String query,
                            OrderStatus status,
                            PaymentStatus paymentStatus,
                            MapSqlParameterSource params) {
    StringBuilder where = new StringBuilder(" WHERE 1=1 ");

    if (query != null && !query.isBlank()) {
      where.append("""
        AND (
          o.order_no ILIKE :query
          OR o.user_id::text ILIKE :query
          OR u.full_name ILIKE :query
          OR u.email ILIKE :query
          OR o.ship_full_name ILIKE :query
          OR o.ship_phone ILIKE :query
        )
        """);
      params.addValue("query", "%" + query.trim() + "%");
    }

    if (status != null) {
      where.append(" AND o.status = :status ");
      params.addValue("status", status.name());
    }

    if (paymentStatus != null) {
      where.append(" AND o.payment_status = :paymentStatus ");
      params.addValue("paymentStatus", paymentStatus.name());
    }

    return where.toString();
  }

  private String toOrderBy(AdminPageRequest pageRequest) {
    String field = switch (pageRequest.sortField()) {
      case "orderNo" -> "o.order_no";
      case "totalAmount" -> "o.total_amount";
      case "status" -> "o.status";
      case "paymentStatus" -> "o.payment_status";
      default -> "o.created_at";
    };
    return field + " " + pageRequest.sortDirection().name() + ", o.created_at DESC";
  }

  private static fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod toPaymentMethod(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return fit.iuh.kttkpm_nhom15_be.payments.domain.models.PaymentMethod.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  private static OrderStatus toOrderStatus(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return OrderStatus.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  private static PaymentStatus toPaymentStatus(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return PaymentStatus.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
}
