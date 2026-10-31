package com.revShop.orderservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CheckoutResponse(
        String checkoutRef,
        Long paymentId,                  // the Pay / Cancel popup needs this for ONLINE
        String paymentMethod,
        BigDecimal totalAmount,
        LocalDateTime paymentExpiresAt,  // null for COD
        List<OrderResponse> orders       // one per seller (Abhinaya's OrderResponse)
) {}