package com.revShop.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// contextId: Nikitha's checkout has its own client for payment-service too
@FeignClient(name = "payment-service", contextId = "sellerPaymentClient")
public interface SellerPaymentClient {

    // Abhinaya's API: marks a COD payment as paid
    @PostMapping("/internal/payments/settle")
    void settle(@RequestBody SettleRequest request);

    record SettleRequest(String checkoutRef) {}
}