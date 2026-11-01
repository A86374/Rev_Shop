package com.revShop.productservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// user-service's InternalNotificationController: puts a message in the seller's bell
@FeignClient(name = "user-service", contextId = "productNotificationClient")
public interface NotificationClient {

    @PostMapping("/internal/notifications")
    void create(@RequestBody NewNotification request);

    // type is one of user-service's NotificationType values (here LOW_STOCK)
    record NewNotification(Long userId, String type, String message) {}
}
