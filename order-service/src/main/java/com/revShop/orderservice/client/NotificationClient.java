package com.revShop.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// your InternalNotificationController in user-service
@FeignClient(name = "user-service", contextId = "orderNotificationClient")
public interface NotificationClient {

    @PostMapping("/internal/notifications")
    void create(@RequestBody NewNotification request);

    // type is one of your NotificationType values: ORDER_PLACED, ORDER_STATUS, PAYMENT
    record NewNotification(Long userId, String type, String message) {}
}