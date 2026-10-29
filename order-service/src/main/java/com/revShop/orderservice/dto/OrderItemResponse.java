
package com.revShop.orderservice.dto;

import com.revShop.orderservice.entity.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        BigDecimal price,
        Integer quantity,
        BigDecimal lineTotal
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getProductId(), item.getProductName(), item.getPrice(),
                item.getQuantity(), item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
    }
}