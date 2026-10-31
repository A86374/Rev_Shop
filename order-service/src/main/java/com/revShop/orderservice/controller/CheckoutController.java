package com.revShop.orderservice.controller;

import com.revShop.orderservice.dto.CheckoutRequest;
import com.revShop.orderservice.dto.CheckoutResponse;
import com.revShop.orderservice.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutResponse checkout(@RequestHeader("X-User-Id") Long buyerId,
                                     @Valid @RequestBody CheckoutRequest request) {
        return checkoutService.checkout(buyerId, request);
    }
}