package com.revShop.paymentservice.controller;

import com.revShop.paymentservice.dto.CreatePaymentRequest;
import com.revShop.paymentservice.dto.PaymentResponse;
import com.revShop.paymentservice.dto.SettlePaymentRequest;
import com.revShop.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/payments")
@RequiredArgsConstructor
public class InternalPaymentController {

    private final PaymentService paymentService;

    // order-service calls this at checkout
    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.create(request));
    }

    // order-service calls this when a COD checkout is fully delivered (Hasitha's seller orders)
    @PostMapping("/settle")
    public PaymentResponse settle(@Valid @RequestBody SettlePaymentRequest request) {
        return paymentService.settleCod(request.checkoutRef());
    }
}