package com.revShop.productservice.dto;

import java.math.BigDecimal;

// what checkout needs to build the orders: the real price and the seller
public record ReservedItem(
        Long productId,
        String name,
        BigDecimal price,
        Long sellerId,
        Integer quantity
) {}