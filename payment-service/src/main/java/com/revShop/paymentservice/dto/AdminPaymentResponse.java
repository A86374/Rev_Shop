package com.revShop.paymentservice.dto;

import com.revShop.paymentservice.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// like PaymentResponse, plus when the payment was made
public record AdminPaymentResponse(
        Long id,
        String checkoutRef,
        Long buyerId,
        BigDecimal amount,
        String method,
        String status,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {
    public static AdminPaymentResponse from(Payment p) {
        return new AdminPaymentResponse(p.getId(), p.getCheckoutRef(), p.getBuyerId(), p.getAmount(),
                p.getMethod().name(), p.getStatus().name(), p.getCreatedAt(), p.getExpiresAt());
    }
}