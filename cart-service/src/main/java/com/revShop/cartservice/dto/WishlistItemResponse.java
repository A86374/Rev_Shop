package com.revShop.cartservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WishlistItemResponse(
        Long productId,
        String name,          // null when the product was removed or deactivated
        BigDecimal price,
        boolean available,    // active and in stock
        LocalDateTime addedAt
) {}