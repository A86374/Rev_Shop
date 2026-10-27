package com.revShop.paymentservice.dto;

import com.revShop.paymentservice.entity.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotBlank String checkoutRef,
        @NotNull Long buyerId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull PaymentMethod method
) {}