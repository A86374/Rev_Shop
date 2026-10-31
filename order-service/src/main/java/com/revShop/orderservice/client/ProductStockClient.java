package com.revShop.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

// contextId: Nikitha's checkout has its own client for product-service too
@FeignClient(name = "product-service", contextId = "orderStockClient")
public interface ProductStockClient {

    // Nikitha's internal API: puts the quantities back on sale
    @PostMapping("/internal/products/release")
    void release(@RequestBody StockRequest request);

    record StockItem(Long productId, Integer quantity) {}

    record StockRequest(List<StockItem> items) {}
}