package com.revShop.orderservice.controller;

import com.revShop.orderservice.dto.OrderResponse;
import com.revShop.orderservice.entity.OrderStatus;
import com.revShop.orderservice.exception.ApiException;
import com.revShop.orderservice.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    // /api/admin/orders or /api/admin/orders?status=PLACED
    @GetMapping
    public List<OrderResponse> allOrders(@RequestHeader("X-User-Role") String role,
                                         @RequestParam(name = "status", required = false) OrderStatus status) {
        // the gateway already blocks non-admins; this also covers calls straight to port 8084
        if (!"ADMIN".equals(role)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admins can see all orders");
        }
        return adminOrderService.allOrders(status);
    }
}