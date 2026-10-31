package com.revShop.paymentservice.controller;

import com.revShop.paymentservice.dto.AdminPaymentResponse;
import com.revShop.paymentservice.entity.PaymentStatus;
import com.revShop.paymentservice.exception.ApiException;
import com.revShop.paymentservice.service.AdminPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    // /api/admin/payments or /api/admin/payments?status=SUCCESS
    @GetMapping
    public List<AdminPaymentResponse> allPayments(@RequestHeader("X-User-Role") String role,
                                                  @RequestParam(name = "status", required = false) PaymentStatus status) {
        if (!"ADMIN".equals(role)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admins can see all payments");
        }
        return adminPaymentService.allPayments(status);
    }
}