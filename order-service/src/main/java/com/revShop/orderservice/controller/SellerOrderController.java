package com.revShop.orderservice.controller;

import com.revShop.orderservice.dto.OrderResponse;
import com.revShop.orderservice.dto.OrderStatusUpdateRequest;
import com.revShop.orderservice.entity.OrderStatus;
import com.revShop.orderservice.service.SellerOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// US-19: the gateway lets only sellers reach /api/seller/** and puts their id in X-User-Id
@RestController
@RequestMapping("/api/seller/orders")
@RequiredArgsConstructor
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;

    // /api/seller/orders or /api/seller/orders?status=PLACED
    @GetMapping
    public List<OrderResponse> myOrders(@RequestHeader("X-User-Id") Long sellerId,
                                        @RequestParam(name = "status", required = false) OrderStatus status) {
        return sellerOrderService.myOrders(sellerId, status);
    }

    // {"status":"SHIPPED"} or {"status":"DELIVERED"}
    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable("id") Long id,
                                      @RequestHeader("X-User-Id") Long sellerId,
                                      @Valid @RequestBody OrderStatusUpdateRequest request) {
        return sellerOrderService.updateStatus(id, sellerId, request.status());
    }
}
