package com.revShop.orderservice.dto;

import com.revShop.orderservice.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusUpdateRequest(@NotNull OrderStatus status) {}