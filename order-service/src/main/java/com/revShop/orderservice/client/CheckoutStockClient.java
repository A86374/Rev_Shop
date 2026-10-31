package com.revShop.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;

// contextId: Abhinaya's order-service code has its own client for product-service
@FeignClient(name = "product-service", contextId = "checkoutStockClient")
public interface CheckoutStockClient {

    @PostMapping("/internal/products/reserve")
    List<ReservedItem> reserve(@RequestBody StockRequest request);

    @PostMapping("/internal/products/release")
    void release(@RequestBody StockRequest request);

    record StockItem(Long productId, Integer quantity) {}

    record StockRequest(List<StockItem> items) {}

    record ReservedItem(Long productId, String name, BigDecimal price, Long sellerId, Integer quantity) {}
}