package com.revShop.productservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service", contextId = "reviewOrderClient")
public interface OrderCheckClient {

    // your InternalOrderController from Task 2
    @GetMapping("/internal/orders/delivered-check")
    DeliveredCheck deliveredCheck(@RequestParam("buyerId") Long buyerId,
                                  @RequestParam("productId") Long productId);

    record DeliveredCheck(boolean delivered) {
    }
}