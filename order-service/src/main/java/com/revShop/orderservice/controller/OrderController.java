
package com.revShop.orderservice.controller;

import com.revShop.orderservice.dto.OrderResponse;
import com.revShop.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public List<OrderResponse> myOrders(@RequestHeader("X-User-Id") Long buyerId) {
        return orderService.myOrders(buyerId);
    }

    @GetMapping("/{id}")
    public OrderResponse myOrder(@PathVariable("id") Long id,
                                 @RequestHeader("X-User-Id") Long buyerId) {
        return orderService.myOrder(id, buyerId);
    }
}