// File: OrderStatus.java
package fit.iuh.kttkpm_nhom15_be.orders.domain.models;

public enum OrderStatus {
    CREATED,    // Vừa đặt hàng, chưa làm gì cả
    CONFIRMED,  // Đã xác nhận (đã thanh toán hoặc shop gọi xác nhận COD)
    SHIPPING,   // Đang giao hàng
    COMPLETED,  // Giao thành công
    CANCELLED   // Đã hủy
}