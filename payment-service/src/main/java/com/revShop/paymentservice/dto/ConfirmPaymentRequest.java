package com.revShop.paymentservice.dto;

import com.revShop.paymentservice.entity.PaymentAction;
import jakarta.validation.constraints.NotNull;

public record ConfirmPaymentRequest(@NotNull PaymentAction action) {}