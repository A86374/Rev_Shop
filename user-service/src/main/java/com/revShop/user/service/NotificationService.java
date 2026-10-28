package com.revShop.user.service;

import com.revShop.user.dto.CreateNotificationRequest;
import com.revShop.user.dto.NotificationResponse;
import com.revShop.user.dto.UnreadCountResponse;
import com.revShop.user.entity.Notification;
import com.revShop.user.exception.ApiException;
import com.revShop.user.repository.NotificationRepository;
import com.revShop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public NotificationResponse create(CreateNotificationRequest req) {
        if (!userRepository.existsById(req.userId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User " + req.userId() + " not found");
        }
        Notification n = Notification.builder()
                .userId(req.userId())
                .type(req.type())
                .message(req.message().trim())
                .build();
        return NotificationResponse.from(notificationRepository.save(n));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> myNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount(Long userId) {
        return new UnreadCountResponse(notificationRepository.countByUserIdAndReadFalse(userId));
    }

    @Transactional
    public NotificationResponse markRead(Long id, Long userId) {
        Notification n = notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification " + id + " not found"));
        n.setRead(true);
        return NotificationResponse.from(n);
    }

    @Transactional
    public UnreadCountResponse markAllRead(Long userId) {
        notificationRepository.markAllRead(userId);
        return new UnreadCountResponse(0);
    }
}