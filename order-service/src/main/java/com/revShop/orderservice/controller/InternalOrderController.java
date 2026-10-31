package com.revShop.orderservice.controller;

import com.revShop.orderservice.dto.PaymentResultRequest;
import com.revShop.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// /internal is not routed by the gateway: only other services call these
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    // payment-service: after Pay, Cancel or expiry
    @PostMapping("/payment-result")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void paymentResult(@Valid @RequestBody PaymentResultRequest request) {
        orderService.applyPaymentResult(request.checkoutRef(), request.success());
    }

    // product-service: before accepting a review
    @GetMapping("/delivered-check")
    public Map<String, Boolean> deliveredCheck(@RequestParam("buyerId") Long buyerId,
                                               @RequestParam("productId") Long productId) {
        return Map.of("delivered", orderService.hasDelivered(buyerId, productId));
    }
}