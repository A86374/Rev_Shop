package com.revShop.user.dto;

import com.revShop.user.entity.User;

import java.time.LocalDateTime;

// what the admin sees - never includes the password hash
public record AdminUserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String role,
        String businessName,
        boolean active,
        LocalDateTime createdAt
) {
    public static AdminUserResponse from(User u) {
        return new AdminUserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(),
                u.getRole().name(), u.getBusinessName(), u.isActive(), u.getCreatedAt());
    }
}