package com.revShop.user.controller;

import com.revShop.user.dto.NotificationResponse;
import com.revShop.user.dto.UnreadCountResponse;
import com.revShop.user.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> myNotifications(@RequestHeader("X-User-Id") Long userId) {
        return notificationService.myNotifications(userId);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount(@RequestHeader("X-User-Id") Long userId) {
        return notificationService.unreadCount(userId);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable("id") Long id,
                                         @RequestHeader("X-User-Id") Long userId) {
        return notificationService.markRead(id, userId);
    }

    @PatchMapping("/read-all")
    public UnreadCountResponse markAllRead(@RequestHeader("X-User-Id") Long userId) {
        return notificationService.markAllRead(userId);
    }
}