package com.revShop.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;

public record SettlePaymentRequest(@NotBlank String checkoutRef) {}