package com.revShop.paymentservice.dto;

import com.revShop.paymentservice.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        String checkoutRef,
        Long buyerId,
        BigDecimal amount,
        String method,
        String status,
        LocalDateTime expiresAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getCheckoutRef(), p.getBuyerId(), p.getAmount(),
                p.getMethod().name(), p.getStatus().name(), p.getExpiresAt());
    }
}