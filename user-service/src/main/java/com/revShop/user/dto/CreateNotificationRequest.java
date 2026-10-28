package com.revShop.user.dto;

import com.revShop.user.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// sent by other services (order, product, payment)
public record CreateNotificationRequest(
        @NotNull Long userId,
        @NotNull NotificationType type,
        @NotBlank @Size(max = 255) String message
) {}