package com.cedarmarket.orders.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(Long id, String customerEmail, OrderStatus status, Instant createdAt,
                            List<Line> items, BigDecimal total) {

    public record Line(Long productId, String productName, int quantity, BigDecimal unitPrice,
                       BigDecimal lineTotal) {
    }

    public static OrderResponse from(CustomerOrder order) {
        List<Line> lines = order.getItems().stream()
                .map(item -> new Line(item.getProduct().getId(), item.getProduct().getName(),
                        item.getQuantity(), item.getUnitPrice(), item.lineTotal()))
                .toList();
        return new OrderResponse(order.getId(), order.getCustomerEmail(), order.getStatus(),
                order.getCreatedAt(), lines, order.total());
    }
}
