package com.revShop.orderservice.dto;

import com.revShop.orderservice.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String checkoutRef,
        Long buyerId,
        Long sellerId,
        String status,
        String paymentMethod,
        BigDecimal totalAmount,
        String shippingAddress,
        boolean refunded,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(o.getId(), o.getCheckoutRef(), o.getBuyerId(), o.getSellerId(),
                o.getStatus().name(), o.getPaymentMethod().name(), o.getTotalAmount(),
                o.getShippingAddress(), o.isRefunded(), o.getCreatedAt(),
                o.getItems().stream().map(OrderItemResponse::from).toList());
    }
}