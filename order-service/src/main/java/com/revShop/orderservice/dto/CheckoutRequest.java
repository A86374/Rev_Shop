package com.revShop.orderservice.dto;

import com.revShop.orderservice.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheckoutRequest(

        @NotNull(message = "Choose ONLINE or COD")
        PaymentMethod paymentMethod,

        @NotBlank(message = "Delivery address is required")
        @Size(max = 500, message = "Delivery address cannot exceed 500 characters")
        String shippingAddress

) {}