package com.revShop.paymentservice.controller;

import com.revShop.paymentservice.dto.ConfirmPaymentRequest;
import com.revShop.paymentservice.dto.PaymentResponse;
import com.revShop.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{id}/confirm")
    public PaymentResponse confirm(@PathVariable("id") Long id,
                                   @RequestHeader("X-User-Id") Long userId,
                                   @Valid @RequestBody ConfirmPaymentRequest request) {
        return paymentService.confirm(id, userId, request.action());
    }
}