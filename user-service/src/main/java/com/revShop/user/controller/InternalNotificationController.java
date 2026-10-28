package com.revShop.user.controller;

import com.revShop.user.dto.CreateNotificationRequest;
import com.revShop.user.dto.NotificationResponse;
import com.revShop.user.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.create(request));
    }
}