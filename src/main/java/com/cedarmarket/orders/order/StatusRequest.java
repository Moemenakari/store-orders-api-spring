package com.cedarmarket.orders.order;

import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull OrderStatus status) {
}
