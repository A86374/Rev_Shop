package com.revShop.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "cart-service", contextId = "checkoutCartClient")
public interface CheckoutCartClient {

    @GetMapping("/api/cart/{userId}")
    List<CartLine> getCart(@PathVariable("userId") Long userId);

    @DeleteMapping("/api/cart/{userId}")
    void clearCart(@PathVariable("userId") Long userId);

    // the JSON of your CartItem entity
    record CartLine(Long id, Long userId, Long productId, Integer quantity) {}
}