package com.revShop.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service")
public interface OrderClient {

    // order-service: PAYMENT_PENDING orders become PLACED (success) or CANCELLED (failure)
    @PostMapping("/internal/orders/payment-result")
    void paymentResult(@RequestBody PaymentResult result);

    record PaymentResult(String checkoutRef, boolean success) {}
}