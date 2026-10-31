package com.revShop.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@FeignClient(name = "payment-service", contextId = "checkoutPaymentClient")
public interface CheckoutPaymentClient {

    // Abhinaya's /internal/payments (already on main since Tuesday)
    @PostMapping("/internal/payments")
    PaymentInfo create(@RequestBody NewPayment request);

    record NewPayment(String checkoutRef, Long buyerId, BigDecimal amount, String method) {}

    record PaymentInfo(Long id, String checkoutRef, Long buyerId, BigDecimal amount,
                       String method, String status, LocalDateTime expiresAt) {}
}