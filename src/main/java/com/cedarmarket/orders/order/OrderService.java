package com.cedarmarket.orders.order;

import com.cedarmarket.orders.common.BusinessRuleException;
import com.cedarmarket.orders.common.NotFoundException;
import com.cedarmarket.orders.product.Product;
import com.cedarmarket.orders.product.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orders;
    private final ProductRepository products;

    public OrderService(OrderRepository orders, ProductRepository products) {
        this.orders = orders;
        this.products = products;
    }

    public OrderResponse place(OrderRequest request) {
        CustomerOrder order = new CustomerOrder(request.customerEmail());
        for (OrderRequest.Line line : request.items()) {
            Product product = products.findById(line.productId())
                    .orElseThrow(() -> new NotFoundException("Product " + line.productId() + " not found"));
            if (product.getStock() < line.quantity()) {
                throw new BusinessRuleException("Not enough stock for " + product.getName()
                        + " (requested " + line.quantity() + ", available " + product.getStock() + ")");
            }
            product.removeStock(line.quantity());
            order.addItem(new OrderItem(product, line.quantity()));
        }
        return OrderResponse.from(orders.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list(OrderStatus status) {
        List<CustomerOrder> found = status == null ? orders.findAll() : orders.findByStatus(status);
        return found.stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return OrderResponse.from(find(id));
    }

    public OrderResponse changeStatus(Long id, OrderStatus next) {
        CustomerOrder order = find(id);
        OrderStatus current = order.getStatus();
        if (!isAllowed(current, next)) {
            throw new BusinessRuleException("Cannot change order from " + current + " to " + next);
        }
        if (next == OrderStatus.CANCELLED) {
            // Cancelling puts the reserved items back on the shelf.
            order.getItems().forEach(item -> item.getProduct().addStock(item.getQuantity()));
        }
        order.setStatus(next);
        return OrderResponse.from(order);
    }

    private static boolean isAllowed(OrderStatus current, OrderStatus next) {
        return switch (current) {
            case PENDING -> next == OrderStatus.PAID || next == OrderStatus.CANCELLED;
            case PAID -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED, CANCELLED -> false;
        };
    }

    private CustomerOrder find(Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new NotFoundException("Order " + id + " not found"));
    }
}
