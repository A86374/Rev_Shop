package com.revShop.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentResultRequest(
        @NotBlank String checkoutRef,
        @NotNull Boolean success
) {}